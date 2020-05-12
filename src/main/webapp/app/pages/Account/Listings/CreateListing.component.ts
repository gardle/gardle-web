import { Component, Inject, Prop } from 'vue-property-decorator';
import { CreateListing } from '@/shared/model/listing.model';
import ImageUpload from '@/components/inputs/imageUpload.vue';
import { maxLength, maxValue, minLength, minValue, required } from 'vuelidate/lib/validators';
import { EventBus } from '@/shared/eventbus/eventbus';
import GardenFieldService from '@/shared/services/gardenField/gardenfield.service';
import { mixins } from 'vue-class-component';
import { LocationSearch } from '@/mixins/locationSearch';
import { validationMessage } from 'vuelidate-messages';

const DESCRIPTION_LENGTH = 1000;

const validations = {
  required({ $params }, props) {
    return this.$t(`listing.common.messages.validation.${props.fieldName}.required`);
  },
  minValue({ $params }, props) {
    return this.$t(`listing.common.messages.validation.${props.fieldName}.minValue`, [$params.minValue.min]);
  },
  maxValue({ $params }, props) {
    return this.$t(`listing.common.messages.validation.${props.fieldName}.maxValue`, [$params.maxValue.max]);
  },
  minLength({ $params }, props) {
    return this.$t(`listing.common.messages.validation.${props.fieldName}.minLength`, [$params.minLength.min]);
  },
  maxLength({ $params }, props) {
    return this.$t(`listing.common.messages.validation.${props.fieldName}.maxLength`, [$params.maxLength.max]);
  }
};

@Component({
  components: {
    ImageUpload
  },
  validations: {
    listing: {
      name: { required, minLength: minLength(3), maxLength: maxLength(100) },
      description: { required, maxLength: maxLength(DESCRIPTION_LENGTH) },
      sizeInM2: { required, minValue: minValue(0) },
      pricePerM2: { required, minValue: minValue(0) },
      phValue: { minValue: minValue(0), maxValue: maxValue(14) }
    },
    listingAddress: { required }
  }
})
export default class PageCreateListing extends mixins(LocationSearch) {
  @Prop({ type: Object }) readonly editItem: any | undefined;

  listing = new CreateListing();
  listing_images: FormData[] = [];
  /**
   * the selected address object
   */
  listingAddress: any = null;

  /**
   * the maximal description length
   */
  description_length = DESCRIPTION_LENGTH;

  isEditing = false;
  existingImages: object[] = [];

  @Inject('gardenFieldService')
  gardenFieldService: () => GardenFieldService;

  get listingPrice() {
    return this.listing.pricePerM2 && this.listing.sizeInM2
      ? this.$options.filters.formatNumber(this.listing.sizeInM2 * this.listing.pricePerM2 * 30)
      : null;
  }

  get validMessages() {
    return validationMessage(validations, { first: 1 });
  }

  get nameErrors() {
    return this.validMessages(this.$v.listing.name, { fieldName: 'name' });
  }

  get sizeInM2Errors() {
    return this.validMessages(this.$v.listing.sizeInM2, { fieldName: 'sizeInM2' });
  }

  get pricePerM2Errors() {
    return this.validMessages(this.$v.listing.pricePerM2, { fieldName: 'pricePerM2' });
  }

  get phValueErrors() {
    return this.validMessages(this.$v.listing.phValue, { fieldName: 'phValue' });
  }

  get listingAddressErrors() {
    return this.validMessages(this.$v.listingAddress, { fieldName: 'listingAddress' });
  }

  async mounted() {
    if (this.editItem) {
      this.listing = this.editItem;
      this.isEditing = true;

      // reverse geocode location
      try {
        this.listingAddress = await this.reverseGeocode(this.editItem.latitude, this.editItem.longitude);
      } catch (e) {
        EventBus.$emit('add-notification', {
          text: this.$t('listing.common.messages.addressCouldNotBeFoundError'),
          color: 'error',
          close: true
        });
      }

      // get existing images and map to array
      await this.getExistingImages(this.editItem.id);
    } else if (this.$route.name === 'Account:Listings:Edit') {
      this.$router.replace({ name: 'Account:Listings:Create' });
    }
  }

  public addressItem(item) {
    return this.getAddressLine(item.address);
  }

  public handleAddImage(formData, fileName) {
    this.listing_images.push(formData);
  }

  public handleRemoveImage(index) {
    this.listing_images.splice(index, 1);
  }

  public async handleDeleteExistingImage(image) {
    try {
      const path = image.path.split('/');
      const imageName = path[path.length - 1];

      // @ts-ignore
      await this.gardenFieldService().deleteImage(this.listing.id, imageName);

      EventBus.$emit('add-notification', {
        text: this.$t('listing.common.messages.imageDeleteSuccess'),
        color: 'success',
        close: true
      });

      const i = this.existingImages.indexOf(image);
      this.existingImages.splice(i, 1);
    } catch (e) {
      console.error(e);
      EventBus.$emit('add-notification', {
        text: this.$t('listing.common.messages.imageDeleteError'),
        color: 'error',
        close: true
      });
    }
  }

  public async getExistingImages(id) {
    try {
      const imgNames = await this.gardenFieldService().getGardenFieldThumbnails(id);

      this.existingImages = imgNames.map(item => ({
        path: item
      }));
    } catch (e) {
      console.error(e);
    }
  }

  public async submit() {
    this.$v.$touch();

    // check if form is invalid
    if (!this.$v.$invalid) {
      //  put address in listing data
      this.listing.city =
        this.listingAddress.address.city ||
        this.listingAddress.address.region ||
        this.listingAddress.address.county ||
        this.listingAddress.address.state;
      this.listing.latitude = this.listingAddress.lat;
      this.listing.longitude = this.listingAddress.lon;

      // create listing in backend -> await response.
      const hasNewImages = this.listing_images.length > 0;
      if (!this.isEditing) {
        this.createListing(hasNewImages);
      } else {
        this.updateListing(hasNewImages);
      }
    }
  }

  public async createListing(hasNewImages) {
    try {
      const entity = await this.gardenFieldService().create(this.listing);
      const msg = `listing.create.messages.entityCreateSuccess${hasNewImages ? 'WithImages' : ''}`;

      EventBus.$emit('add-notification', {
        text: this.$t(msg),
        color: 'success',
        timeout: 1000,
        close: true
      });

      if (hasNewImages) {
        await this.uploadImages(entity.id);
      }

      this.$router.push('/account/listings');
    } catch (e) {
      console.error(e);
    }
  }

  public async updateListing(hasNewImages) {
    try {
      const entity = await this.gardenFieldService().update(this.listing);
      const msg = `listing.update.messages.entityUpdateSuccess${hasNewImages ? 'WithImages' : ''}`;

      EventBus.$emit('add-notification', {
        text: this.$t(msg),
        color: 'info',
        timeout: 1000,
        close: true
      });

      if (hasNewImages) {
        await this.uploadImages(entity.id);
      }

      this.$router.push('/account/listings');
    } catch (e) {
      console.error(e);
    }
  }

  public uploadImages(id) {
    return new Promise<any>((resolve, reject) => {
      const prs = [];
      for (let i = 0; i < this.listing_images.length; ++i) {
        prs.push(this.gardenFieldService().uploadImage(id, this.listing_images[i]));
      }

      Promise.all(prs)
        .then(res => {
          EventBus.$emit('add-notification', {
            text: this.$t('listing.common.messages.imageUploadSuccess'),
            color: 'success',
            primaryAction: {
              icon: 'mdi-check'
            }
          });

          resolve();
        })
        .catch(err => {
          console.error(err);
          EventBus.$emit('add-notification', {
            text: this.$t('listing.common.messages.imageUploadError'),
            color: 'error',
            primaryAction: {
              icon: 'mdi-close'
            }
          });
          reject(err);
        });
    });
  }
}
