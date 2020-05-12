import { createLocalVue, shallowMount, Wrapper } from '@vue/test-utils';
import axios from 'axios';
import VueRouter from 'vue-router';
import Vuex from 'vuex';
import Vue from 'vue';

import store from '@/store/';
import i18n from '@/shared/config/i18n';
import * as config from '@/shared/config/config';
import * as vuetifyConfig from '@/shared/config/config-vuetify';
import router from '@/router/index';
import ListingItemComponent from '@/components/account/listings/listingItem.component';
import ListingItem from '@/components/account/listings/listingItem.vue';
import LeasingService from '@/shared/services/leasing/leasing.service';
import GardenFieldService from '@/shared/services/gardenField/gardenfield.service';

const localVue = createLocalVue();
localVue.use(VueRouter);
localVue.use(Vuex);

const mockedAxios = axios as jest.Mocked<typeof axios>;

config.initVueApp(localVue);
const vuetify = vuetifyConfig.initVuetify(localVue);

const defaultMount = (compProps: object = null) => {
  return shallowMount<ListingItemComponent>(ListingItem, {
    store,
    i18n,
    localVue,
    router,
    vuetify,
    sync: false,
    propsData: compProps,
    provide: {
      leasingService: () => new LeasingService(),
      gardenFieldService: () => new GardenFieldService()
    }
  });
};

const mockedListing = {
  id: 19,
  name: 'Treeflex',
  description: 'Molestiae ullam repudiandae sint totam vero. Ea deleniti ab non.',
  sizeInM2: 7.77,
  pricePerM2: 39.51,
  latitude: 47.822188,
  longitude: 15.320493,
  city: 'East Rosella',
  roofed: true,
  glassHouse: true,
  high: false,
  water: true,
  electricity: true,
  phValue: null,
  owner: {
    id: 3,
    login: 'johnnie.koss1',
    firstName: 'Helga',
    lastName: 'Abshire',
    email: 'johnnie.koss1@gmail.com'
  },
  imageUrl: null
};

const testLeasing1 = {
  id: 2,
  from: '2020-01-25T23:00:00Z',
  to: '2020-02-16T22:59:00Z',
  status: 'OPEN',
  user: {
    id: 55,
    login: 'testuser',
    firstName: 'Test',
    lastName: 'User',
    email: 'testmail@mail.com'
  },
  messages: [],
  gardenField: {
    id: 1,
    name: 'Testfield'
  }
};

const mockedImages = ['de324-23e42-aef32-ef1a3-b3c7a.png', 'b45ca-67def-a32e8-56ac1-112ac.png', '12af3-43ea5-45ef2-32ae5-98ef1.png'];

const clearMocks = () => {
  mockedAxios.get.mockReset();
  mockedAxios.post.mockReset();
  mockedAxios.put.mockReset();
};

const mockListingImages = () => {
  mockedAxios.get.mockReturnValueOnce(
    Promise.resolve({
      data: mockedImages
    })
  );
};

describe('listingItem component', () => {
  let wrapper: Wrapper<ListingItemComponent>;

  beforeEach(async () => {
    clearMocks();
    mockListingImages();

    wrapper = defaultMount({
      listing: mockedListing
    });

    await Vue.nextTick();
  });

  it('should fetch images correctly', () => {
    expect(wrapper.vm.image_urls).toEqual(
      mockedImages.map(item => 'api/v1/gardenfields/' + mockedListing.id + '/downloadThumbnail/' + item)
    );
  });

  it('should fetch item leasings', async () => {
    mockedAxios.get.mockReturnValueOnce(
      Promise.resolve({
        data: {
          content: [testLeasing1]
        }
      })
    );

    await wrapper.vm.fetchItemLeasings();

    expect(wrapper.vm.itemLeasings).toEqual([testLeasing1]);
  });

  it('should update item leasing to reserved', async () => {
    mockedAxios.put.mockReturnValueOnce(
      Promise.resolve({
        data: {}
      })
    );

    const leasingCopy = Object.assign({}, testLeasing1);
    wrapper.vm.itemLeasings = [leasingCopy];

    await wrapper.vm.updateLeasingStatus(wrapper.vm.itemLeasings[0], 'RESERVED');

    expect(wrapper.vm.itemLeasings[0].status).toBe('RESERVED');
  });

  it('should update item leasing to rejected', async () => {
    mockedAxios.put.mockReturnValueOnce(
      Promise.resolve({
        data: {}
      })
    );

    const leasingCopy = Object.assign({}, testLeasing1);
    wrapper.vm.itemLeasings = [leasingCopy];

    await wrapper.vm.updateLeasingStatus(wrapper.vm.itemLeasings[0], 'REJECTED');

    expect(wrapper.vm.itemLeasings[0].status).toBe('REJECTED');
  });
});
