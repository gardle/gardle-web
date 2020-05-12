import { Component, Inject, Prop } from 'vue-property-decorator';
import Vue from 'vue';
import { EventBus } from '@/shared/eventbus/eventbus';
import { IGardenField } from '@/shared/model/gardenfield.model';
import LeasingService from '@/shared/services/leasing/leasing.service';
import GardenFieldService from '@/shared/services/gardenField/gardenfield.service';
import ImageGallery from '@/pages/ListingDetail/ImageGallery/ImageGallery.vue';
import NewMessagePopup from '@/components/Messages/NewMessagePopup.vue';
import { ILeasingDateRange } from '@/shared/model/leasingDateRange.model';
import Map from '@/components/maps/map.vue';
import { differenceInDays, isBefore, parseISO } from 'date-fns';
import PaymentService from '@/shared/services/payment.service';
import TranslationService from '@/locale/translation.service';

@Component({
  components: {
    ImageGallery,
    NewMessagePopup,
    Map
  }
})
export default class PageListingDetail extends Vue {
  @Prop()
  fieldId: number;

  gardenField: IGardenField = null;
  fieldImages: string[] = null;
  fieldThumbnails: string[] = null;
  leasedDateRanges: ILeasingDateRange[] = [];

  @Inject('gardenFieldService')
  private gardenFieldService: () => GardenFieldService;

  @Inject('paymentService')
  private paymentService: () => PaymentService;

  @Inject('leasingService')
  private leasingService: () => LeasingService;

  @Inject('translationService')
  private translationService: () => TranslationService;

  bookingRange = [];

  locale = this.translationService().getLocale();

  mounted() {
    EventBus.$emit('update-navigation', null);

    const stripeScript = document.createElement('script');
    stripeScript.setAttribute('src', 'https://js.stripe.com/v3/');
    document.head.appendChild(stripeScript);
  }

  async created() {
    try {
      this.gardenField = await this.gardenFieldService().getGardenField(this.fieldId);
      this.fieldImages = await this.gardenFieldService().getGardenFieldImages(this.fieldId);
      this.fieldThumbnails = await this.gardenFieldService().getGardenFieldThumbnails(this.fieldId);
      this.leasedDateRanges = await this.leasingService().getLeasingDateRanges(this.fieldId);
    } catch (e) {
      if (e instanceof Error) {
        let err_data = null;
        switch (e.name) {
          default:
            err_data = { title_key: 'UNKNOWN_ERROR' };
        }

        EventBus.$emit('add-notification', {
          text: this.$t('error.' + err_data.title_key),
          color: 'error',
          close: true,
          timeout: 6000
        });
      }
    }
  }

  async bookField() {
    try {
      const dates = this.sortBookingDatesAsc();

      // create a checkout session in backend
      // redirect user to stripe checkout
      // create leasing if successful

      const sessionId = await this.paymentService()
        .createCheckoutSession(dates[0], dates[1], this.gardenField)
        .then(res => {
          console.log(res);
          return res.id;
        });

      // @ts-ignore
      const stripe = new Stripe('pk_test_wG5kbql9y64A2JGFAhlineGl00nOOcPub6');
      const { error } = await stripe.redirectToCheckout({
        sessionId
      });

      EventBus.$emit('add-notification', {
        text: this.$t('leasing.messages.created', [this.$d(dates[0], 'short'), this.$d(dates[1], 'short')]),
        color: 'success',
        close: true,
        timeout: 6000
      });
    } catch (e) {
      console.error(e);
    }
  }

  sortBookingDatesAsc() {
    const dates = [parseISO(this.bookingRange[0]), parseISO(this.bookingRange[1])];
    if (!isBefore(dates[0], dates[1])) {
      dates.reverse();
    }

    return dates;
  }

  allowedDates(val: Date): boolean {
    for (const dateRange of this.leasedDateRanges) {
      if (val >= dateRange.from && val <= dateRange.to) {
        return false;
      }
    }
    return true;
  }

  get selectedPrice() {
    const dates = this.sortBookingDatesAsc();
    return dates[0] && dates[1] ? differenceInDays(dates[1], dates[0]) * this.gardenField.pricePerM2 * this.gardenField.sizeInM2 : null;
  }

  get fullOwnerName(): string {
    return this.gardenField.owner.firstName + ' ' + this.gardenField.owner.lastName;
  }

  get name(): string {
    return this.gardenField.name;
  }

  get city(): string {
    return this.gardenField.city;
  }

  get description(): string {
    return this.gardenField.description;
  }

  get fieldFeatures() {
    return {
      size: {
        text: this.$t('listing.common.form.label.sizeSqm'),
        icon: 'mdi-crop-free',
        type: Number,
        value: this.gardenField.sizeInM2,
        unit: 'm²'
      },
      water: {
        text: this.$t('listing.common.form.label.water'),
        icon: 'mdi-water',
        type: Boolean,
        value: this.gardenField.water
      },
      electricity: {
        text: this.$t('listing.common.form.label.electricity'),
        icon: 'mdi-flash',
        type: Boolean,
        value: this.gardenField.electricity
      },
      covered: {
        text: this.$t('listing.common.form.label.glassHouse'),
        icon: 'mdi-home-roof',
        type: Boolean,
        value: this.gardenField.glassHouse
      },
      phValue: {
        text: this.$t('listing.common.form.label.phValue'),
        icon: 'mdi-gauge',
        type: Number,
        value: this.gardenField.phValue
      }
    };
  }

  public openMsgPopup() {
    EventBus.$emit('newMsgPopup');
  }

  public amIOwner() {
    return this.gardenField.owner.id === this.$store.getters.userId;
  }
}
