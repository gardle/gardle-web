import { createLocalVue, shallowMount, Wrapper } from '@vue/test-utils';
import Vue from 'vue';
import VueRouter from 'vue-router';
import axios from 'axios';

import router from '@/router';
import i18n from '@/shared/config/i18n';
import * as config from '@/shared/config/config';
import * as vuetifyConfig from '@/shared/config/config-vuetify';
import ListingsComponent from '@/pages/Account/Listings/Listings.component';
import Listings from '@/pages/Account/Listings/Listings.vue';
import GardenFieldService from '@/shared/services/gardenField/gardenfield.service';
import AccountService from '@/shared/services/auth/account.service';
import PaymentService from '@/shared/services/payment.service';

const localVue = createLocalVue();
localVue.use(VueRouter);

const mockedAxios: any = axios;

config.initVueApp(localVue);
const vuetify = vuetifyConfig.initVuetify(localVue);

const clone = items => items.map(item => (Array.isArray(item) ? clone(item) : item));

const validListings = [
  {
    id: 1,
    name: 'Test field',
    description: 'Description',
    sizeInM2: 250.0,
    pricePerM2: 0.75,
    latitude: 40.20385545,
    longitude: 11.3412990053915,
    city: 'Wien',
    roofed: false,
    glassHouse: false,
    high: true,
    water: false,
    electricity: true,
    phValue: null,
    owner: {
      id: 51,
      login: 'user',
      firstName: 'User',
      lastName: 'User',
      email: 'user@localhost'
    }
  },
  {
    id: 2,
    name: 'Test me',
    description: 'Some description',
    sizeInM2: 350.0,
    pricePerM2: 1.4,
    latitude: 48.90567245,
    longitude: 15.4957191266501,
    city: 'Vöcklabruck',
    roofed: false,
    glassHouse: false,
    high: false,
    water: true,
    electricity: true,
    phValue: null,
    owner: {
      id: 51,
      login: 'user',
      firstName: 'User',
      lastName: 'User',
      email: 'user@localhost'
    }
  },
  {
    id: 3,
    name: 'Test field',
    description: 'hello 123',
    sizeInM2: 300.0,
    pricePerM2: 1.2,
    latitude: 43.98026265,
    longitude: 23.7767031742826,
    city: 'Gmunden',
    roofed: false,
    glassHouse: true,
    high: false,
    water: false,
    electricity: true,
    phValue: null,
    owner: {
      id: 51,
      login: 'user',
      firstName: 'User',
      lastName: 'User',
      email: 'user@localhost'
    }
  }
];
const clonedListings = clone(validListings);

describe('Listings Component', () => {
  let wrapper: Wrapper<ListingsComponent>;
  let listingsPage: ListingsComponent;

  beforeEach(async () => {
    mockedAxios.get.mockReset();
    mockedAxios.post.mockReset();
    mockedAxios.delete.mockReset();

    mockedAxios.get
      .mockResolvedValueOnce({
        data: {
          stripeAccountVerified: true
        }
      })
      .mockResolvedValueOnce({
        data: {
          content: clonedListings
        }
      });

    wrapper = shallowMount<ListingsComponent>(Listings, {
      i18n,
      localVue,
      router,
      vuetify,
      sync: false,
      provide: {
        gardenFieldService: () => new GardenFieldService(),
        paymentService: () => new PaymentService(),
        accountService: () => new AccountService()
      }
    });
    listingsPage = wrapper.vm;

    await Vue.nextTick();
  });

  it('should set the users gardenfields', () => {
    expect(listingsPage.listings).toEqual(validListings);
  });

  it('should handle create listing click', () => {
    const spy = jest.spyOn(router, 'push');

    listingsPage.handleCreateListingClick();

    expect(spy).toHaveBeenCalledWith({
      name: 'Account:Listings:Create',
      params: {},
      path: '/account/listings/create'
    });
  });

  it('should handle update listing click', () => {
    const spy = jest.spyOn(router, 'push');

    listingsPage.handleEditListing(validListings[0]);

    expect(spy).toHaveBeenCalledWith({
      name: 'Account:Listings:Edit',
      params: {
        editItem: validListings[0]
      },
      path: '/account/listings/edit'
    });
  });

  it('should handle delete listing click', async () => {
    mockedAxios.delete.mockReturnValue(
      Promise.resolve({
        data: {}
      })
    );

    await listingsPage.handleDeleteListing(clonedListings[0].id);

    expect(mockedAxios.delete).toBeCalledWith(`/api/v1/gardenfields/${validListings[0].id}`);

    await Vue.nextTick();

    expect(listingsPage.listings).toHaveLength(2);
    expect(listingsPage.listings).toEqual([validListings[1], validListings[2]]);
  });
});
