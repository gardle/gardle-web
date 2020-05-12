import { createLocalVue, shallowMount, Wrapper } from '@vue/test-utils';
import axios from 'axios';
import VueRouter from 'vue-router';
import Vuex from 'vuex';
import store from '@/store/';
import i18n from '@/shared/config/i18n';
import * as config from '@/shared/config/config';
import * as vuetifyConfig from '@/shared/config/config-vuetify';
import router from '@/router/index';
import FilterBarComponent from '@/components/filter/FilterBar.component';
import FilterBar from '@/components/filter/FilterBar.vue';
import SearchService from '@/shared/services/gardenField/search/search.service';
import { GardenFieldFilterCriteria } from '@/shared/model/gardenFieldFilterCriteria.model';
import { LocationFilterCriteria } from '@/shared/model/locationFilterCritera.model';
import { LocationSearch } from '@/mixins/locationSearch';

const localVue = createLocalVue();
localVue.use(VueRouter);
localVue.use(Vuex);

const mockedAxios = axios as jest.Mocked<typeof axios>;

config.initVueApp(localVue);
const vuetify = vuetifyConfig.initVuetify(localVue);
const mockGeolocation = {
  getCurrentPosition: jest.fn(),
  watchPosition: jest.fn()
};

describe('Filter Component', () => {
  let wrapper: Wrapper<FilterBarComponent>;
  let filterBarComponent: FilterBarComponent;
  const filterData = new GardenFieldFilterCriteria(null, null, null, null, null, null, null, null, null);

  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.get.mockReturnValue(Promise.resolve({}));
    mockedAxios.get.mockReset();
    wrapper = shallowMount<FilterBarComponent>(FilterBar, {
      store,
      i18n,
      localVue,
      router,
      vuetify,
      sync: false,
      mixins: [LocationSearch],
      provide: {
        searchService: () => new SearchService()
      },
      mocks: {
        navigator: mockGeolocation
      }
    });
    filterBarComponent = wrapper.vm;
  });

  it('should be a Vue instance', () => {
    expect(wrapper.isVueInstance()).toBeTruthy();
  });

  // it('should set defaults and validation correctly', () => {
  //   expect(filterBarComponent.filterCriteria.locationFilterCriteria.radius).toBe(20);
  //   expect(filterBarComponent.showLeasingDateMenu).toBe(false);
  //   expect(filterBarComponent.filterCriteria.locationFilterCriteria.latitude).toBe(null);
  //   expect(filterBarComponent.filterCriteria.locationFilterCriteria.longitude).toBe(null);
  // });

  // it('should emit update filter event correctly', async () => {
  //   const filterCriteria = new GardenFieldFilterCriteria();
  //   filterCriteria.locationFilterCriteria = new LocationFilterCriteria();
  //   filterCriteria.roofed = true;
  //   filterCriteria.minPricePerM2 = 10;
  //   filterCriteria.maxPricePerM2 = 100;
  //
  //   filterBarComponent.filterCriteria = filterCriteria;
  //   await filterBarComponent.handleUpdateFilters();
  //
  //   expect(wrapper.emitted('update-filters')).toBeTruthy();
  // });

  // it('should reject invalid filter', async () => {
  //   const invalidMinPrice = -1;
  //   filterBarComponent.filterCriteria = new GardenFieldFilterCriteria();
  //   filterBarComponent.leasingTime = null;
  //   filterBarComponent.size = [invalidMinPrice, -10];
  //   filterBarComponent.price = [invalidMinPrice, 100];
  //   filterBarComponent.showLeasingDateMenu = null;
  //   // filterBarComponent.searchKeywords = null;
  //   // filterBarComponent.latitude = null;
  //   // filterBarComponent.longitude = null;
  //
  //   expect(filterBarComponent.$v.price.$invalid).toBe(true);
  //   expect(filterBarComponent.$v.size.$invalid).toBe(true);
  // });

  // it('should successfully validate valid filter', async () => {
  //   filterBarComponent.filterCriteria = null;
  //   filterBarComponent.radius = null;
  //   filterBarComponent.leasingTime = null;
  //   filterBarComponent.size = [0, 0];
  //   filterBarComponent.price = [0, 0];
  //   filterBarComponent.showLeasingDateMenu = null;
  //   filterBarComponent.searchKeywords = null;
  //   filterBarComponent.latitude = null;
  //   filterBarComponent.longitude = null;
  //
  //   expect(filterBarComponent.$v.price.$invalid).toBe(false);
  //   expect(filterBarComponent.$v.size.$invalid).toBe(false);
  //
  //   filterBarComponent.size = [10, 100];
  //   filterBarComponent.price = [10, 100];
  //
  //   expect(filterBarComponent.$v.price.$invalid).toBe(false);
  //   expect(filterBarComponent.$v.size.$invalid).toBe(false);
  // });

  // it('should correctly reset filters', async () => {
  //   filterBarComponent.handleResetFilters();
  //
  //   expect(filterBarComponent.leasingTime).toEqual([]);
  //   expect(filterBarComponent.size).toEqual([0, 100]);
  //   expect(filterBarComponent.price).toEqual([0, 100]);
  //   expect(filterBarComponent.showLeasingDateMenu).toEqual(false);
  //   expect(filterBarComponent.searchKeywords).toEqual(null);
  //   expect(filterBarComponent.locationString).toEqual(null);
  //   expect(filterBarComponent.latitude).toEqual(null);
  //   expect(filterBarComponent.longitude).toEqual(null);
  //   expect(filterBarComponent.radius).toEqual(20);
  // });
});
