import { createLocalVue, shallowMount, Wrapper } from '@vue/test-utils';
import axios from 'axios';
import VueRouter from 'vue-router';
import Vuex from 'vuex';
import store from '@/store/';
import i18n from '@/shared/config/i18n';
import * as config from '@/shared/config/config';
import * as vuetifyConfig from '@/shared/config/config-vuetify';
import router from '@/router/index';
import PageHome from '@/pages/Home/Home.component';
import Home from '@/pages/Home/Home.vue';
import SearchService from '@/shared/services/gardenField/search/search.service';

const localVue = createLocalVue();
localVue.use(VueRouter);
localVue.use(Vuex);

const mockedAxios: any = axios;

config.initVueApp(localVue);
const vuetify = vuetifyConfig.initVuetify(localVue);

jest.mock('axios', () => ({
  get: jest.fn(),
  post: jest.fn()
}));

describe('Home Component', () => {
  let wrapper: Wrapper<PageHome>;
  let homeComp: PageHome;

  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.get.mockReturnValue(Promise.resolve({}));
    mockedAxios.post.mockReset();

    wrapper = shallowMount<PageHome>(Home, {
      store,
      i18n,
      localVue,
      router,
      vuetify,
      sync: false,
      provide: {
        searchService: () => new SearchService()
      }
    });
    homeComp = wrapper.vm;
  });

  it('should be a Vue instance', () => {
    expect(wrapper.isVueInstance()).toBeTruthy();
  });

  it('should set all default values correctly', () => {
    expect(homeComp.gardenFieldSearchTimer).toBe(null);
    expect(homeComp.searchVal).toBe(null);
    expect(homeComp.gardenFieldItems).toEqual([]);
    expect(homeComp.selectedGardenField).toBe(null);
  });

  it('should autocomplete', async () => {
    mockedAxios.post.mockReturnValue(
      Promise.resolve({
        data: {},
        status: 200
      })
    );

    const searchVal = 'test autocomplete val';
    homeComp.searchVal = searchVal;

    await homeComp.searchGardenField();

    expect(mockedAxios.get).toHaveBeenCalledWith('api/v1/gardenfields/autocomplete?partialSearchString=' + searchVal);
  });

  it('should handle autocomplete error', async () => {
    mockedAxios.post.mockReturnValue(
      Promise.reject({
        data: {},
        status: 400
      })
    );

    const call = async () => {
      return await homeComp.searchGardenField();
    };

    // TODO expect(call()).rejects;
  });

  it('should push route for all search correctly', () => {
    const spy = jest.spyOn(router, 'push');
    homeComp.handleExploreSearch();

    expect(spy).toHaveBeenCalledWith({ path: '/search' });
  });

  it('should push route for search correctly', () => {
    const spy = jest.spyOn(router, 'push');
    const testSearchVal = 'test search val';
    homeComp.searchVal = testSearchVal;

    homeComp.handleSearch();

    expect(spy).toHaveBeenCalledWith({
      path: `/search?keywords=${testSearchVal}`
    });
  });
});
