import { Component, Inject } from 'vue-property-decorator';
import Vue from 'vue';
import LeasingService from '@/shared/services/leasing/leasing.service';

@Component({})
export default class PageDashboard extends Vue {
  requests: any = null;
  currentLeasings: any = null;

  @Inject('leasingService')
  private leasingService: () => LeasingService;

  async created() {
    this.requests = await this.fetchListingRequests();
    this.currentLeasings = await this.fetchActiveLeasings();
  }

  private async fetchUserListingLeasings(filters: object) {
    try {
      return await this.leasingService().getUserListingLeasings(filters);
    } catch (e) {
      console.error(e);
    }
  }

  private async fetchListingRequests() {
    return await this.fetchUserListingLeasings({
      leasingStatus: 'OPEN',
      number: 1,
      size: 5
    });
  }

  private async fetchActiveLeasings() {
    return await this.fetchUserListingLeasings({
      leasingStatus: 'RESERVED',
      state: 'ONGOING',
      number: 1,
      size: 5
    });
  }

  public async acceptLeasingRequest(leasingId: number, gardenFieldId: number) {
    try {
      await this.leasingService().acceptLeasingRequest(leasingId, gardenFieldId);
      this.removeItemFromList(this.requests.content as [], 'id', leasingId);

      this.requests = await this.fetchListingRequests();
    } catch (e) {
      console.error(e);
    }
  }

  public async rejectLeasingRequest(leasingId: number, gardenFieldId: number) {
    try {
      await this.leasingService().rejectLeasingRequest(leasingId, gardenFieldId);
      this.removeItemFromList(this.requests.content as [], 'id', leasingId);

      this.requests = await this.fetchListingRequests();
    } catch (e) {
      console.error(e);
    }
  }

  public removeItemFromList(list: [], idField = 'id', itemId: number) {
    list.splice(list.indexOf(list.filter(item => item[idField] === itemId)[0]), 1);
  }
}
