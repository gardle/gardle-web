import { Component, Inject, Prop } from 'vue-property-decorator';
import Vue from 'vue';
import ImageCompressorService from '@/shared/services/util/image.compressor.service';
import { IMAGE_COMPRESSION_MAXSIZE, IMAGE_COMPRESSION_QUALITY } from '@/constants';
import { EventBus } from '@/shared/eventbus/eventbus';

@Component
export default class ImageUpload extends Vue {
  @Prop({ default: () => 'image/gif,image/jpeg,image/png,image/bmp,image/jpg' })
  readonly acceptTypes: string;

  @Prop({ default: () => [] })
  readonly existingImages: [];

  @Inject('imageCompressorService')
  imageCompressorService: () => ImageCompressorService;

  uploaderId = 'image-uploader';

  images: object[] = [];

  public addImage(e) {
    const files = e.target.files || e.dataTransfer.files;
    if (!files.length) {
      return false;
    }

    const createPromises = [];
    for (let i = 0; i < files.length; ++i) {
      createPromises.push(this.createImage(files[i]));
    }

    Promise.all(createPromises).catch(reason => {
      EventBus.$emit('add-notification', {
        text: this.$t('error.' + reason.title_key),
        color: 'error',
        close: true,
        timeout: 5000
      });
    });

    if (document.getElementById(this.uploaderId)) {
      // @ts-ignore
      document.getElementById(this.uploaderId).value = [];
    }
  }

  public async createImage(file) {
    const reader = new FileReader();
    const formData = new FormData();
    const compressedFile = await this.imageCompressorService().compressImage({
      imageFile: file,
      quality: IMAGE_COMPRESSION_QUALITY,
      maxSize: IMAGE_COMPRESSION_MAXSIZE
    });
    formData.append('image', compressedFile);
    reader.onload = e => {
      // @ts-ignore
      const dataURI = e.target.result;
      if (dataURI) {
        this.images.push({ name: compressedFile.name, path: dataURI });
        this.$emit('added-image', formData, compressedFile.name);
      }
    };
    reader.readAsDataURL(file);
  }

  public deleteImage(currentIndex) {
    this.images.splice(currentIndex, 1);
    this.$emit('removed-image', currentIndex);
  }

  public deleteExistingImage(image) {
    this.$emit('delete-existing-image', image);
  }
}
