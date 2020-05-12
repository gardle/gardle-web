import { createLocalVue, shallowMount, Wrapper } from '@vue/test-utils';
import VueRouter from 'vue-router';
import Vuex from 'vuex';
import axios from 'axios';

import store from '@/store/';
import i18n from '@/shared/config/i18n';
import * as config from '@/shared/config/config';
import * as vuetifyConfig from '@/shared/config/config-vuetify';
import SettingsComponent from '@/pages/Account/Settings/Settings.component';
import SettingsPage from '@/pages/Account/Settings/Settings.vue';
import router from '@/router';
import AccountService from '@/shared/services/auth/account.service';

const localVue = createLocalVue();
localVue.use(VueRouter);
localVue.use(Vuex);

const mockedAxios: any = axios;

config.initVueApp(localVue);
const vuetify = vuetifyConfig.initVuetify(localVue);

const testAccount = {
  id: 1234567,
  login: 'user',
  firstName: 'VName',
  lastName: 'NName',
  email: 'test@user.com',
  tel: '+4367761244368',
  birthDate: '1990-01-01',
  activated: true,
  langKey: 'en',
  authorities: [],
  createdBy: 'admin',
  createdDate: new Date(),
  lastModifiedBy: 'admin',
  lastModifiedDate: new Date(),
  password: 'passwdhash'
};

jest.mock('axios', () => ({
  get: jest.fn(),
  post: jest.fn()
}));

describe('Settings Component', () => {
  let settings: SettingsComponent;
  let wrapper: Wrapper<SettingsComponent>;

  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.get.mockReturnValue(Promise.resolve(testAccount));
    mockedAxios.post.mockReset();

    wrapper = shallowMount<SettingsComponent>(SettingsPage, {
      store,
      i18n,
      localVue,
      router,
      vuetify,
      sync: false,
      provide: {
        accountService: () => new AccountService()
      }
    });
    settings = wrapper.vm;
    settings.account = testAccount;
  });

  it('should be a Vue instance', () => {
    expect(wrapper.isVueInstance()).toBeTruthy();
  });

  it('should set error when passwords do no match', () => {
    settings.passwordChange.currentPassword = 'abcde012345';
    settings.passwordChange.newPassword = 'anypassword123';
    settings.passwordChange.repeatPassword = 'anyotherpasswd1234';

    settings.$v.passwordChange.newPassword.$touch();
    settings.$v.passwordChange.repeatPassword.$touch();

    expect(settings.$v.passwordChange.repeatPassword.$error).toBe(true);
  });

  it('should set error when firstName is too long', () => {
    settings.account = testAccount;
    settings.account.firstName = 'sdlkfdshfjaldsfurhioandkfldksjfhakjsdlhfljskdfdslfk';
    settings.$v.account.firstName.$touch();
    expect(settings.$v.account.firstName.$error).toBe(true);
  });

  it('should set error when lastName is too long', () => {
    settings.account = testAccount;
    settings.account.lastName = 'sdlkfdshfjaldsfurhioandkfldksjfhakjsdlhfljskdfdslfk';
    settings.$v.account.lastName.$touch();
    expect(settings.$v.account.lastName.$error).toBe(true);
  });

  it('should set error when phone number is invalid', () => {
    settings.account = testAccount;
    settings.account.tel = '++abc4334554';

    settings.$v.account.tel.$touch();

    expect(settings.$v.account.tel.$error).toBe(true);
  });

  it('should set error when age(birthdate) < 18', () => {
    settings.account = testAccount;
    const birthDate: Date = new Date();
    birthDate.setFullYear(birthDate.getFullYear() - 10);
    settings.account.birthDate = birthDate.toISOString();
    settings.$v.account.birthDate.$touch();
    expect(settings.$v.account.birthDate.$error).toBe(true);
  });

  it('should call change-password when passwords match', async () => {
    mockedAxios.post.mockReturnValue(
      Promise.resolve({
        data: {},
        status: 201
      })
    );

    settings.passwordChange.currentPassword = 'abcde012345';
    settings.passwordChange.newPassword = 'anypassword123';
    settings.passwordChange.repeatPassword = 'anypassword123';

    settings.$v.passwordChange.$touch();
    expect(settings.$v.passwordChange.$invalid).toBe(false);
    await settings.changePassword();

    expect(mockedAxios.post).toHaveBeenCalledWith('/api/v1/account/change-password', {
      currentPassword: settings.passwordChange.currentPassword,
      newPassword: settings.passwordChange.newPassword
    });
  });
});
