import { createLocalVue, shallowMount, Wrapper } from '@vue/test-utils';
import axios from 'axios';
import VueRouter from 'vue-router';
import Vuex from 'vuex';

import router from '@/router/index';
import store from '@/store/';
import i18n from '@/shared/config/i18n';
import * as config from '@/shared/config/config';
import Activate from '@/pages/auth/Activate/Activate.vue';
import ActivateComponent from '@/pages/auth/Activate/Activate.component';
import ActivationService from '@/shared/services/auth/activation.service';
import * as vuetifyConfig from '@/shared/config/config-vuetify';

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

describe('Activate Component', () => {
  let wrapper: Wrapper<ActivateComponent>;
  let activate: ActivateComponent;

  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.get.mockReturnValue(Promise.resolve({}));
    mockedAxios.post.mockReset();

    wrapper = shallowMount<ActivateComponent>(Activate, {
      store,
      i18n,
      localVue,
      router,
      vuetify,
      sync: false,
      provide: {
        activationService: () => new ActivationService()
      }
    });
    activate = wrapper.vm;
  });

  it('should be a Vue instance', () => {
    expect(wrapper.isVueInstance()).toBeTruthy();
  });

  it('should provide correct defaults', () => {
    expect(activate.activated).toBe(null);
    expect(activate.loading).toBe(false);
  });

  it('should activate account', async () => {
    mockedAxios.get.mockReturnValue(Promise.resolve({}));

    const key = 'validActivationKey';
    await activate.init(key);

    expect(activate.activated).toBe(true);
    expect(activate.loading).toBe(false);
  });

  it('should reject account activation', async () => {
    mockedAxios.get.mockReturnValue(
      Promise.reject({
        response: {
          status: 500
        }
      })
    );

    const key = 'invalidActivationKey';
    await activate.init(key);

    expect(activate.activated).toBe(false);
    expect(activate.loading).toBe(false);
  });
});
