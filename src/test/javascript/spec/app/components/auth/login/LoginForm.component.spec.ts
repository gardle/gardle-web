import { createLocalVue, shallowMount, Wrapper } from '@vue/test-utils';
import axios from 'axios';
import VueRouter from 'vue-router';
import Vuex from 'vuex';

import store, { genDefaultState } from '@/store/';
import i18n from '@/shared/config/i18n';
import * as config from '@/shared/config/config';
import Login from '@/components/auth/login/loginForm.vue';
import LoginComponent from '@/components/auth/login/loginForm.component';
import AuthenticationService from '@/shared/services/auth/authentication.service';
import * as vuetifyConfig from '@/shared/config/config-vuetify';
import router from '@/router/index';
import { GARDLE_TOKEN_NAME } from '@/constants';

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

describe('Login Component', () => {
  let wrapper: Wrapper<LoginComponent>;
  let login: LoginComponent;
  const loginData = {
    username: 'testuser',
    password: 'testpass1234'
  };

  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.get.mockReturnValue(Promise.resolve({}));
    mockedAxios.post.mockReset();
    localStorage.removeItem(GARDLE_TOKEN_NAME);

    wrapper = shallowMount<LoginComponent>(Login, {
      store,
      i18n,
      localVue,
      router,
      vuetify,
      sync: false,
      provide: {
        authenticationService: () => new AuthenticationService()
      }
    });
    login = wrapper.vm;
  });

  afterEach(() => {
    store.replaceState(genDefaultState());
  });

  it('should be a Vue instance', () => {
    expect(wrapper.isVueInstance()).toBeTruthy();
  });

  it('should set defaults and validation correctly', () => {
    expect(login.username).toBe(null);
    expect(login.password).toBe(null);

    expect(login.$v.username.$invalid).toBe(true);
    expect(login.$v.password.$invalid).toBe(true);
  });

  it('should provide required username error', async () => {
    login.username = null;
    login.password = loginData.password;

    await login.submit();

    expect(login.$v.username.$error).toBe(true);
    expect(login.usernameErrors).not.toBe('');
  });

  it('should provide required password error', async () => {
    login.username = loginData.username;
    login.password = null;

    await login.submit();

    expect(login.$v.password.$error).toBe(true);
    expect(login.passwordErrors).not.toBe('');
  });

  it('should do login correctly', async () => {
    const spy = jest.spyOn(router, 'push');

    const idToken = 'eehrhsa.someRandomIdToken';
    mockedAxios.post.mockReturnValue(
      Promise.resolve({
        status: 200,
        data: {
          id_token: idToken
        }
      })
    );

    login.username = loginData.username;
    login.password = loginData.password;

    await login.submit();

    expect(mockedAxios.post).toBeCalledWith('/api/v1/authenticate', {
      username: loginData.username,
      password: loginData.password
    });
    expect(wrapper.vm.$store.state.user.token).toBe(idToken);
    expect(spy).toHaveBeenCalledWith({
      name: 'Account:Dashboard',
      params: {},
      path: '/account/dashboard'
    });
  });

  it('should reject invalid login', async () => {
    mockedAxios.post.mockReturnValue(
      Promise.reject({
        response: {
          status: 401
        }
      })
    );

    const invalidPass = 'invalidPass';
    login.username = loginData.username;
    login.password = invalidPass;

    await login.submit();

    expect(mockedAxios.post).toBeCalledWith('/api/v1/authenticate', {
      username: loginData.username,
      password: invalidPass
    });
    expect(login.$store.state.user.token).toBe(null);
  });
});
