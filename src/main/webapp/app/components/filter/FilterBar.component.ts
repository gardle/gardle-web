import { Component, Inject } from 'vue-property-decorator';
import { GardenFieldFilterCriteria, IGardenFieldFilterCriteria } from '@/shared/model/gardenFieldFilterCriteria.model';
import { validationMixin } from 'vuelidate';
import { validMinMaxPositiveNumericParameterArray } from '@/shared/validations';
import { LocationFilterCriteria } from '@/shared/model/locationFilterCritera.model';
import { LocationSearch } from '@/mixins/locationSearch';
import { mixins } from 'vue-class-component';
import { EventBus } from '@/shared/eventbus/eventbus';
import { IGardenFieldFilterBoundaries } from '@/shared/model/gardenFieldFilterBoundaries.model';
import SearchService from '@/shared/services/gardenField/search/search.service';
import { parseISO } from 'date-fns';
import { parseUriComponents } from '@/shared/services/util/uriComponents.service';

const signUpFormValidations = {
  validSizeParameterArray({ $params }, props) {
    return this.$t(`filterBar.messages.validation.size.validMinMaxNumericParameter`);
  },
  validPriceParameterArray({ $params }, props) {
    return this.$t(`filterBar.messages.validation.price.validMinMaxNumericParameter`);
  }
};

const getCleanFilters = () => ({
  keywords: null,
  locationString: null,
  lat: null,
  long: null,
  radius: null,
  leasingTime: [],
  price: [0, 0],
  size: [0, 0],
  roofed: null,
  water: null,
  electricity: null,
  high: null,
  glassHouse: null
});

@Component({
  mixins: [validationMixin],
  validations: {
    size: { validMinMaxPositiveNumericParameterArray },
    price: { validMinMaxPositiveNumericParameterArray }
  }
})
export default class FilterBar extends mixins(LocationSearch) {
  filters = getCleanFilters();

  showLeasingDateMenu = false;
  filterBoundaries: IGardenFieldFilterBoundaries = null;

  @Inject('searchService')
  private searchService: () => SearchService;

  async mounted() {
    this.syncFilters();

    this.filterBoundaries = await this.searchService().getFilterBoundaries();
    this.checkFilterBoundaries();
    if (this.filters.locationString) {
      await this.setCoordinatesFromLocationString();
    }
    this.handleUpdateFilters();
  }

  /**
   * fetch filters from URL and map them to the filters object
   */
  syncFilters() {
    this.filters = Object.assign({}, this.filters, parseUriComponents(this.$route.query));
  }

  /**
   *  Check whether the given boundaries are valid, otherwise reset to valid value
   */
  checkFilterBoundaries() {
    this.filters.price = this.checkBoundary(this.filters.price, this.filterBoundaries.minPrice, this.filterBoundaries.maxPrice);
    this.filters.size = this.checkBoundary(this.filters.size, this.filterBoundaries.minSize, this.filterBoundaries.maxSize);
  }

  checkBoundary(arr, min, max) {
    const vals = [...arr];
    vals.forEach((val, i) => {
      if (val < min) {
        vals[i] = min;
      } else if (val > max) {
        vals[i] = max;
      }
    });
    return vals;
  }

  public handleGetMyLocation() {
    navigator.geolocation.getCurrentPosition(
      async position => {
        this.filters.lat = position.coords.latitude;
        this.filters.long = position.coords.longitude;
        await this.getReverseGeolocationForCoordinates();
      },
      err => {
        EventBus.$emit('add-notification', {
          text: this.$t('filterBar.messages.error.addressCouldNotBeFoundError'),
          color: 'error',
          close: true
        });
      }
    );
  }

  public async handleUpdateFilters() {
    this.$router.push({ query: this.mapFiltersToQuery() });

    if (this.filters.locationString && (!this.filters.lat || !this.filters.long)) {
      await this.setCoordinatesFromLocationString();
    }

    this.$emit('update-filters', this.mapFiltersToBackendFilters());
  }

  public mapFiltersToQuery() {
    // @ts-ignore
    return Object.entries(this.filters).reduce((a, [k, v]) => (v === null || v === 'null' || !v ? a : { ...a, [k]: v }), {});
  }

  /**
   * map the internal filter object to the desired interface
   */
  public mapFiltersToBackendFilters() {
    const leasingBoundaries = this.sortDatesAsc(this.filters.leasingTime);
    const locationFilters = new LocationFilterCriteria(this.filters.lat, this.filters.long, this.filters.radius);
    return new GardenFieldFilterCriteria(
      locationFilters,
      this.filters.price[0],
      this.filters.price[1],
      this.filters.size[0],
      this.filters.size[1],
      this.filters.roofed,
      this.filters.electricity,
      this.filters.high,
      this.filters.glassHouse,
      this.filters.water,
      leasingBoundaries[0],
      leasingBoundaries[1],
      this.filters.keywords
    );
  }

  public sortDatesAsc(arr) {
    arr = arr.map(item => parseISO(item));
    if (arr[1] < arr[0]) {
      return arr.reverse();
    }
    return arr;
  }

  public async handleResetFilters() {
    this.filterBoundaries = await this.searchService().getFilterBoundaries();
    this.resetFilterbarFields();
    this.handleUpdateFilters();
  }

  async clearLocation() {
    this.filters.locationString = null;
    this.filters.lat = null;
    this.filters.long = null;
    this.filters.radius = null;
    this.handleUpdateFilters();
  }

  private resetFilterbarFields() {
    this.filters = getCleanFilters();
  }

  public resetLeasingTimeMenu() {
    this.filters.leasingTime = [];
  }

  protected async getReverseGeolocationForCoordinates() {
    try {
      const res = await this.reverseGeocode(this.filters.lat, this.filters.long);

      this.setLocation(res);
    } catch (e) {
      EventBus.$emit('add-notification', {
        text: this.$t('filterBar.messages.error.addressCouldNotBeFoundError'),
        color: 'error',
        close: true
      });
    }
  }

  private setLocation(item) {
    this.filters.lat = item.lat;
    this.filters.long = item.lon;
    this.filters.locationString = this.getAddressLine(item.address);
  }

  private async setCoordinatesFromLocationString() {
    try {
      const res = await this.forwardGeocode(this.filters.locationString);
      this.setLocation(res[0]);
    } catch (e) {
      EventBus.$emit('add-notification', {
        text: this.$t('filterBar.messages.error.locationNotFound'),
        color: 'error',
        close: true
      });
    }
  }
}
