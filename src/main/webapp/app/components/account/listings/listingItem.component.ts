import { Component, Inject, Prop } from 'vue-property-decorator';
import Vue from 'vue';
import axios from 'axios';
import { PageModel } from '@/shared/model/page.model';
import LeasingService from '@/shared/services/leasing/leasing.service';
import LeasingStatusSwitcher from '@/components/leasings/LeasingStatusSwitcher.vue';
import { IMAGE_BASE_URL } from '@/constants';
import GardenFieldService from '@/shared/services/gardenField/gardenfield.service';

@Component({
  components: {
    LeasingStatusSwitcher
  }
})
export default class ListingItem extends Vue {
  @Prop({ type: Object, required: true }) readonly listing: any;

  @Inject('leasingService')
  private leasingService: () => LeasingService;
  @Inject('gardenFieldService')
  private gardenFieldService: () => GardenFieldService;

  image_urls = [];

  statuses = [
    {
      status: 'OPEN',
      color: 'info'
    },
    {
      status: 'RESERVED',
      color: 'success'
    },
    {
      status: 'REJECTED',
      color: 'error'
    },
    {
      status: 'CANCELLED',
      color: 'warning'
    }
  ];

  itemLeasingsLoading = false;
  showItemLeasings = false;
  itemLeasings = [];
  itemLeasingsPage = new PageModel(0, 5);

  itemLeasingsFilters = {};

  mounted() {
    this.getListingImages();
  }

  get leasingSectionOpen() {
    return this.showItemLeasings && this.itemLeasings;
  }

  get isLastPage() {
    return this.itemLeasings.length === 0;
  }

  get isFirstPage() {
    return this.itemLeasingsPage.pageNumber === 0;
  }

  public nextPage() {
    if (this.itemLeasings.length > 0) {
      this.itemLeasingsPage.pageNumber += 1;
      this.fetchItemLeasings();
    }
  }

  public prevPage() {
    if (this.itemLeasingsPage.pageNumber > 0) {
      this.itemLeasingsPage.pageNumber -= 1;
      this.fetchItemLeasings();
    }
  }

  public async getListingImages() {
    this.image_urls = await this.gardenFieldService().getGardenFieldThumbnails(this.listing.id);
  }

  public async toggleItemLeasings() {
    this.showItemLeasings = !this.showItemLeasings;

    if (this.showItemLeasings === false || this.itemLeasings.length > 0) {
      return;
    } else {
      this.fetchItemLeasings();
    }
  }

  public async fetchItemLeasings() {
    this.itemLeasingsLoading = true;

    await this.leasingService()
      .getForGardenField(this.itemLeasingsPage, this.listing.id, this.itemLeasingsFilters)
      .then(res => {
        this.itemLeasingsLoading = false;
        this.itemLeasings = res.content;
      });
  }

  public async updateLeasingStatus(leasing, status) {
    try {
      switch (status) {
        case 'RESERVED':
          await this.leasingService().acceptLeasingRequest(leasing.id, leasing.gardenField.id);
          break;
        case 'REJECTED':
          await this.leasingService().rejectLeasingRequest(leasing.id, leasing.gardenField.id);
          break;
      }

      leasing.status = status;
    } catch (e) {
      console.error(e);
    }
  }
}
