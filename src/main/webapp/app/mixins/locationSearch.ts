import axios from 'axios';
import { Component, Watch } from 'vue-property-decorator';
import Vue from 'vue';

const externalAxios = axios.create();

const locationIqKey = process.env.LOCATION_IQ_KEY;
const locationIqClient = {
  searchAddress(q) {
    return externalAxios.get(
      `https://eu1.locationiq.com/v1/search.php?key=${locationIqKey}&q=${encodeURIComponent(
        q
      )}&format=json&accept-language=de&addressdetails=1`
    );
  },
  reverseCodeAddress(lat, lon) {
    return externalAxios.get(
      `https://eu1.locationiq.com/v1/reverse.php?key=${locationIqKey}` +
        `&lat=${lat}&lon=${lon}&format=json&accept-language=de&addressdetails=1`
    );
  }
};

@Component
export class LocationSearch extends Vue {
  /**
   * represents the user input (which is geocoded into machine-readable attributes
   */
  locationSearchString = null;
  locationSearchTimer = null;
  items = [];

  isLocationSearchLoading = false;

  get addresses() {
    return this.items.filter((item: any) => {
      return (
        !['tourism', 'office', 'shop'].includes(item.class) &&
        (item.address.road || item.address.hamlet) &&
        item.address.house_number &&
        item.address.postcode &&
        item.address.state
      );
    });
  }

  @Watch('locationSearchString', { immediate: true })
  private debounceSearch(e) {
    if (!e || e.length <= 3) {
      return;
    }
    // cancel pending call
    clearTimeout(this.locationSearchTimer);

    // delay new call 500ms
    this.locationSearchTimer = setTimeout(() => {
      this.forwardGeocode(e);
    }, 500);
  }

  protected forwardGeocode(q) {
    return new Promise<any>((resolve, reject) => {
      this.isLocationSearchLoading = true;
      locationIqClient
        .searchAddress(q)
        .then(res => {
          this.isLocationSearchLoading = false;
          this.items = res.data;
          resolve(res.data);
        })
        .catch(err => {
          this.isLocationSearchLoading = false;
          console.error(err);
          reject(err);
        });
    });
  }

  protected reverseGeocode(lat, lon): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      this.isLocationSearchLoading = true;
      locationIqClient
        .reverseCodeAddress(lat, lon)
        .then(res => {
          this.isLocationSearchLoading = false;
          this.items = [res.data];
          resolve(res.data);
        })
        .catch(err => {
          this.isLocationSearchLoading = false;
          console.error(err);
          reject(err);
        });
    });
  }

  protected getAddressLine(address) {
    return address
      ? `${address.road || address.hamlet || address.path} ${address.house_number || ', ' + address.village}, ${address.postcode ||
          ''} ${address.state || address.country}`
      : '';
  }
}
