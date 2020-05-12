import { createLocalVue, shallowMount, Wrapper } from '@vue/test-utils';
import axios from 'axios';
import VueRouter from 'vue-router';
import Vuex from 'vuex';
import store from '@/store/';
import i18n from '@/shared/config/i18n';
import * as config from '@/shared/config/config';
import * as vuetifyConfig from '@/shared/config/config-vuetify';
import router from '@/router/index';
import PageSearchResults from '@/pages/SearchResults/SearchResults.component';
import SearchResults from '@/pages/SearchResults/SearchResults.vue';
import SearchService from '@/shared/services/gardenField/search/search.service';
import { LocationSearch } from '@/mixins/locationSearch';

const localVue = createLocalVue();
localVue.use(VueRouter);
localVue.use(Vuex);

const mockedAxios = axios as jest.Mocked<typeof axios>;

config.initVueApp(localVue);
const vuetify = vuetifyConfig.initVuetify(localVue);

const clone = items => items.map(item => (Array.isArray(item) ? clone(item) : item));

const validOverviewElements = [
  {
    id: 1,
    name: 'Test field',
    description: 'Description',
    sizeInM2: 250.0,
    pricePerM2: 0.75
  },
  {
    id: 2,
    name: 'Test me',
    description: 'Some description',
    sizeInM2: 350.0,
    pricePerM2: 1.4
  },
  {
    id: 3,
    name: 'Test field',
    description: 'hello 123',
    sizeInM2: 300.0,
    pricePerM2: 1.2
  }
];
const clonedOverviewElements = clone(validOverviewElements);
const defaultCleanMocks = () => {
  mockedAxios.get.mockReset();
  mockedAxios.post.mockReset();
  mockedAxios.delete.mockReset();
};

describe('SearchResults Component', () => {
  let wrapper: Wrapper<PageSearchResults>;
  let searchResultsComp: PageSearchResults;

  beforeEach(() => {
    defaultCleanMocks();

    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        data: {
          content: clonedOverviewElements
        }
      })
    );

    wrapper = shallowMount<PageSearchResults>(SearchResults, {
      store,
      i18n,
      localVue,
      router,
      vuetify,
      sync: false,
      mixins: [LocationSearch],
      provide: {
        searchService: () => new SearchService()
      }
    });
    searchResultsComp = wrapper.vm;
  });

  it('should be a Vue instance', () => {
    expect(wrapper.isVueInstance()).toBeTruthy();
  });

  it('should set gardenfields correctly', async () => {
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        data: clonedOverviewElements
      })
    );

    await searchResultsComp.handleFilterUpdate(null);

    expect(searchResultsComp.gardenFieldPage.content).not.toBe(null);
  });

  it('should push route correctly', () => {
    const spy = jest.spyOn(router, 'push');
    const gf = 100;

    searchResultsComp.showListingDetail(gf);

    expect(spy).toHaveBeenCalledWith('/fields/' + gf);
  });
});
