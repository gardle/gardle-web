import { Component, Prop } from 'vue-property-decorator';
import Vue from 'vue';

@Component({})
export default class ImageGalleryComponent extends Vue {
  @Prop()
  images: string[];

  @Prop()
  thumbnails: string[];

  dialogOpen = false;

  imageIndex = 0;
  pageIndex = 0;

  get DialogOpen() {
    return this.dialogOpen;
  }

  set DialogOpen(value: boolean) {
    this.dialogOpen = value;
  }

  get ImageIndex(): number {
    return this.imageIndex;
  }

  set ImageIndex(value: number) {
    this.imageIndex = value;
    this.PageIndex = Math.floor(value / this.imageGroupCount);
  }

  get PageIndex(): number {
    return this.pageIndex;
  }

  set PageIndex(value: number) {
    this.pageIndex = value;
  }

  get splicesImages(): string[][] {
    if (this.thumbnails) {
      const splicesImages = [];
      for (let i = 0; i < this.thumbnails.length; i += this.imageGroupCount) {
        const pageIndex = i / this.imageGroupCount;
        splicesImages[pageIndex] = this.thumbnails.slice(i, i + this.imageGroupCount);
      }
      return splicesImages;
    }
  }

  get imageGroupCount(): number {
    return 2 + (this.$vuetify.breakpoint.smAndUp ? 1 : 0) + (this.$vuetify.breakpoint.mdAndUp ? 1 : 0);
  }

  imageClick(pageIndex: number, imageIndex: number) {
    this.imageIndex = pageIndex * this.imageGroupCount + imageIndex;
    this.dialogOpen = true;
  }

  closeDialog() {
    this.dialogOpen = false;
  }
}
