import { Component, Inject } from 'vue-property-decorator';
import Vue from 'vue';
import LeasingService from '@/shared/services/leasing/leasing.service';
import { PageModel } from '@/shared/model/page.model';
import { differenceInDays, isAfter, isBefore, isFuture, isPast, parseISO } from 'date-fns';
import GardenFieldService from '@/shared/services/gardenField/gardenfield.service';

@Component
export default class PageLeasings extends Vue {
  @Inject('leasingService')
  leasingService: () => LeasingService;

  @Inject('gardenFieldService')
  gardenFieldService: () => GardenFieldService;

  leasingPage = new PageModel(0, 10);
  totalLeasingPages = 0;
  leasings = [];

  selectedLeasing: object = null;

  detailLeasingOpen = false;

  mounted() {
    this.fetchLeasings();
  }

  get hasPagination() {
    return this.totalLeasingPages > 1;
  }

  get isFirstPage() {
    return this.leasingPage.pageNumber === 0;
  }

  get isLastPage() {
    return this.leasingPage.pageNumber >= this.totalLeasingPages;
  }

  fetchLeasings() {
    this.leasingService()
      .getForOwner(this.leasingPage, this.$store.getters.userId)
      .then(res => {
        console.log(res);
        this.leasings = res.content;
        this.totalLeasingPages = res.totalPages;
      });
  }

  nextPage() {
    this.leasingPage.pageNumber += 1;
    this.fetchLeasings();
  }

  prevPage() {
    this.leasingPage.pageNumber -= 1;
    this.fetchLeasings();
  }

  isLeasingCurrent(from, to) {
    const now = new Date();
    return isBefore(parseISO(from), now) && isAfter(parseISO(to), now);
  }

  isLeasingInFuture(from) {
    return isFuture(parseISO(from));
  }

  hasLeasingEnded(to) {
    return isPast(parseISO(to));
  }

  isLeasingCancellable(leasing) {
    return leasing.status === 'OPEN' && differenceInDays(parseISO(leasing.from), new Date()) >= 14;
  }

  cancelLeasing(item) {
    this.leasingService()
      .cancelLeasingRequest(item.id, item.gardenField.id)
      .then(res => {
        item.status = 'CANCELLED';
      });
  }

  showLeasingDetails(leasing) {
    // fetch gardenFieldDetails
    // calc Price for duration
    this.gardenFieldService()
      .getGardenField(leasing.gardenField.id)
      .then(res => {
        leasing.gardenField = res;

        this.selectedLeasing = leasing;
        this.detailLeasingOpen = true;
      });
  }
}
