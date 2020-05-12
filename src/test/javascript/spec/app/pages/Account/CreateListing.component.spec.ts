import { createLocalVue, shallowMount, Wrapper } from '@vue/test-utils';
import axios from 'axios';
import VueRouter from 'vue-router';
import Vuex from 'vuex';

import store from '@/store/';
import i18n from '@/shared/config/i18n';
import * as config from '@/shared/config/config';
import CreateListing from '@/pages/Account/Listings/CreateListing.vue';
import CreateListingComponent from '@/pages/Account/Listings/CreateListing.component';
import GardenFieldService from '@/shared/services/gardenField/gardenfield.service';
import * as vuetifyConfig from '@/shared/config/config-vuetify';
import router from '@/router/index';
import Vue from 'vue';
import { LocationSearch } from '@/mixins/locationSearch';

const localVue = createLocalVue();
localVue.use(VueRouter);
localVue.use(Vuex);

const mockedAxios = axios as jest.Mocked<typeof axios>;

config.initVueApp(localVue);
const vuetify = vuetifyConfig.initVuetify(localVue);

const defaultMount = (compProps: object = null) => {
  return shallowMount<CreateListingComponent>(CreateListing, {
    store,
    i18n,
    localVue,
    router,
    vuetify,
    sync: false,
    propsData: compProps,
    mixins: [LocationSearch],
    provide: {
      gardenFieldService: () => new GardenFieldService()
    }
  });
};

const defaultCleanMocks = () => {
  mockedAxios.get.mockReset();
  mockedAxios.post.mockReset();
  mockedAxios.delete.mockReset();
};

const existingItem = {
  id: 1,
  name: 'some sunny gardenfield',
  description: 'some description',
  sizeInM2: 132.7,
  pricePerM2: 2.47,
  latitude: 48.1935896,
  longitude: 16.3656036,
  city: 'Vienna',
  roofed: true,
  glassHouse: false,
  high: true,
  water: true,
  electricity: true,
  phValue: 6.2,
  owner: {
    id: 51,
    login: 'maxi1997',
    firstName: 'max',
    lastName: 'musterman',
    email: 'max1997@gmail.com'
  }
};
const editImgNames = ['img01.png', 'testimg.png', 'img_random.png'];
const mockedReturnAddress = {
  displayName: 'Some retrieved testaddress',
  latitude: 48.1935896,
  longitude: 16.3656036,
  class: 'building',
  address: {
    city: 'Wien City',
    region: 'Wien Region',
    state: 'Wien State'
  }
};
const validCreateItem = {
  name: 'some sunny gardenfield',
  description: 'some description',
  sizeInM2: 132.7,
  pricePerM2: 2.47,
  roofed: true,
  glassHouse: false,
  high: true,
  water: true,
  electricity: true,
  phValue: 6.2
};

describe('CreateListing component', () => {
  let wrapper: Wrapper<CreateListingComponent>;

  it('should be a Vue instance', () => {
    defaultCleanMocks();
    wrapper = defaultMount();
    expect(wrapper.isVueInstance()).toBeTruthy();
  });

  it('should default mount in create mode', () => {
    defaultCleanMocks();
    wrapper = defaultMount();

    expect(wrapper.vm.isEditing).toBe(false);
  });

  it('should mount into editing correctly', async () => {
    defaultCleanMocks();
    mockedAxios.get
      .mockReturnValueOnce(
        Promise.resolve({
          data: mockedReturnAddress
        })
      )
      .mockReturnValueOnce(
        Promise.resolve({
          data: editImgNames
        })
      );

    wrapper = defaultMount({
      editItem: existingItem
    });

    await Vue.nextTick();

    expect(wrapper.vm.isEditing).toBe(true);
    expect(wrapper.vm.listingAddress).toBe(mockedReturnAddress);
  });

  it('should validate each input correctly', () => {
    wrapper = defaultMount();

    wrapper.vm.listing.name = null;
    wrapper.vm.$v.listing.name.$touch();
    // @ts-ignore
    expect(wrapper.vm.$v.listing.name.required).toBe(false);

    wrapper.vm.listing.sizeInM2 = null;
    wrapper.vm.$v.listing.sizeInM2.$touch();
    // @ts-ignore
    expect(wrapper.vm.$v.listing.sizeInM2.required).toBe(false);

    wrapper.vm.listing.sizeInM2 = -1;
    wrapper.vm.$v.listing.sizeInM2.$touch();
    // @ts-ignore
    expect(wrapper.vm.$v.listing.sizeInM2.minValue).toBe(false);

    wrapper.vm.listing.pricePerM2 = null;
    wrapper.vm.$v.listing.pricePerM2.$touch();
    // @ts-ignore
    expect(wrapper.vm.$v.listing.pricePerM2.required).toBe(false);

    wrapper.vm.listing.pricePerM2 = -0.1;
    wrapper.vm.$v.listing.pricePerM2.$touch();
    // @ts-ignore
    expect(wrapper.vm.$v.listing.pricePerM2.minValue).toBe(false);

    wrapper.vm.listing.phValue = -1;
    wrapper.vm.$v.listing.phValue.$touch();
    // @ts-ignore
    expect(wrapper.vm.$v.listing.phValue.minValue).toBe(false);

    wrapper.vm.listing.phValue = 15;
    wrapper.vm.$v.listing.phValue.$touch();
    // @ts-ignore
    expect(wrapper.vm.$v.listing.phValue.maxValue).toBe(false);

    wrapper.vm.listingAddress = null;
    wrapper.vm.$v.listingAddress.$touch();
    // @ts-ignore
    expect(wrapper.vm.$v.listingAddress.required).toBe(false);
  });

  it('should handle add/remove image correctly', () => {
    wrapper = defaultMount();

    const img = new FormData();
    img.set('img', 'img1');

    const img2 = new FormData();
    img2.set('img', 'img2');

    wrapper.vm.handleAddImage(img, 'test-img.png');

    expect(wrapper.vm.listing_images.length).toEqual(1);

    wrapper.vm.handleAddImage(img2, 'test-img2.png');
    wrapper.vm.handleRemoveImage(0);

    expect(wrapper.vm.listing_images.length).toEqual(1);
    expect(wrapper.vm.listing_images[0].get('img')).toEqual('img2');
  });

  it('should handle remove existing image correctly', async () => {
    defaultCleanMocks();

    mockedAxios.get
      .mockReturnValueOnce(
        Promise.resolve({
          data: mockedReturnAddress
        })
      )
      .mockReturnValueOnce(
        Promise.resolve({
          data: editImgNames
        })
      );
    mockedAxios.delete.mockReturnValue(Promise.resolve());

    wrapper = defaultMount({
      editItem: existingItem
    });

    const img_name = 'image-name.png';
    const img = {
      path: `https://some-address.com/images/${img_name}`
    };

    wrapper.vm.existingImages = [img];
    await wrapper.vm.handleDeleteExistingImage(img);

    expect(mockedAxios.delete).toHaveBeenCalledWith(`/api/v1/gardenfields/${existingItem.id}/${img_name}`);
    expect(wrapper.vm.existingImages).toEqual([]);
  });

  it('should map existing images correctly', async () => {
    defaultCleanMocks();
    mockedAxios.get
      .mockReturnValueOnce(
        Promise.resolve({
          data: mockedReturnAddress
        })
      )
      .mockReturnValueOnce(
        Promise.resolve({
          data: editImgNames
        })
      );

    wrapper = defaultMount({
      editItem: existingItem
    });

    // await all calls to the mocked backend
    await Vue.nextTick();
    await Vue.nextTick();
    await Vue.nextTick();

    /* expect(wrapper.vm.existingImages).toEqual(
      editImgNames.map(img => ({
        path: `api/v1/gardenfields/${existingItem.id}/downloadThumbnail/${img}`
      }))
    ); TODO */
  });

  it('should create only if valid and return correctly', async () => {
    defaultCleanMocks();

    mockedAxios.post.mockReturnValue(
      Promise.resolve({
        data: existingItem
      })
    );

    wrapper = defaultMount();

    // @ts-ignore
    wrapper.vm.listing = validCreateItem;
    wrapper.vm.listingAddress = mockedReturnAddress;

    await wrapper.vm.submit();

    expect(mockedAxios.post).toHaveBeenCalledWith('/api/v1/gardenfields/', validCreateItem);
  });

  it('should update only if valid and return correctly', async () => {
    const updatedItem = Object.assign({}, existingItem);
    updatedItem.name = 'Updated Field Name';
    defaultCleanMocks();
    mockedAxios.get
      .mockReturnValueOnce(
        Promise.resolve({
          data: mockedReturnAddress
        })
      )
      .mockReturnValueOnce(
        Promise.resolve({
          data: editImgNames
        })
      );

    mockedAxios.put.mockReturnValue(
      Promise.resolve({
        data: updatedItem
      })
    );

    wrapper = defaultMount({
      editItem: existingItem
    });

    await Vue.nextTick();

    wrapper.vm.listing = updatedItem;

    await wrapper.vm.submit();

    expect(mockedAxios.put).toHaveBeenCalledWith('/api/v1/gardenfields/', updatedItem);
  });
});
