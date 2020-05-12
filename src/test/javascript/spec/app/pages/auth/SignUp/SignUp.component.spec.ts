import { createLocalVue, shallowMount, Wrapper } from '@vue/test-utils';
import axios from 'axios';
import VueRouter from 'vue-router';
import Vuex from 'vuex';

import store from '@/store/';
import i18n from '@/shared/config/i18n';
import * as config from '@/shared/config/config';
import SignUp from '@/pages/auth/SignUp/SignUp.vue';
import SignUpComponent from '@/pages/auth/SignUp/SignUp.component';
import AuthenticationService from '@/shared/services/auth/authentication.service';
import * as vuetifyConfig from '@/shared/config/config-vuetify';
import { SignUpUser } from '@/shared/model/signUpUser.model';
import router from '@/router/index';

const localVue = createLocalVue();
localVue.use(VueRouter);
localVue.use(Vuex);

const mockedAxios: any = axios;

config.initVueApp(localVue);
const vuetify = vuetifyConfig.initVuetify(localVue);

describe('SignUp Component', () => {
  let wrapper: Wrapper<SignUpComponent>;
  let signUp: SignUpComponent;
  const signUpAccount = new SignUpUser(
    'name',
    'user',
    'testusername',
    'test@email.com',
    '1995-05-03',
    '098423942371',
    'testpass1234',
    'TESTIBAN1234'
  );

  const fillSignUpForm = (comp: SignUpComponent) => {
    comp.firstname = signUpAccount.firstName;
    comp.lastname = signUpAccount.lastName;
    comp.username = signUpAccount.login;
    comp.email = signUpAccount.email;
    comp.birthdate = signUpAccount.birthDate;
    comp.tel = signUpAccount.tel;
    comp.password = signUpAccount.password;
    comp.passwordConfirm = signUpAccount.password;
    comp.gtcAccept = true;
    comp.iban = signUpAccount.bankAccountIBAN;
  };

  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.get.mockReturnValue(Promise.resolve({}));
    mockedAxios.post.mockReset();

    wrapper = shallowMount<SignUpComponent>(SignUp, {
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
    signUp = wrapper.vm;
  });

  it('should be a Vue instance', () => {
    expect(wrapper.isVueInstance()).toBeTruthy();
  });

  it('should set all default values correctly', () => {
    expect(signUp.firstname).toBe(null);
    expect(signUp.lastname).toBe(null);
    expect(signUp.email).toBe(null);
    expect(signUp.username).toBe(null);
    expect(signUp.tel).toBe(null);
    expect(signUp.birthdate).toBe(null);
    expect(signUp.password).toBe(null);
    expect(signUp.eric).toBe(false);
    expect(signUp.passwordConfirm).toBe(null);
    expect(signUp.gtcAccept).toBe(null);
    expect(signUp.iban).toBe(null);

    expect(signUp.firstnameErrors).toBe('');
    expect(signUp.lastnameErrors).toBe('');
    expect(signUp.emailErrors).toBe('');
    expect(signUp.usernameErrors).toBe('');
    expect(signUp.telErrors).toBe('');
    expect(signUp.birthdateErrors).toBe('');
    expect(signUp.passwordErrors).toBe('');
    expect(signUp.passwordConfirmErrors).toBe('');
    expect(signUp.gtcAcceptErrors).toBe('');
    expect(signUp.ibanErrors).toBe('');
  });

  it('should set error when passwords do no match', () => {
    signUp.password = signUpAccount.password;
    signUp.passwordConfirm = 'invalidPass';

    signUp.$v.passwordConfirm.$touch();

    expect(signUp.$v.passwordConfirm.$error).toBe(true);
  });

  it('should set error when username is invalid', () => {
    signUp.username = 'testuser$/';
    signUp.$v.username.$touch();
    expect(signUp.$v.username.$error).toBe(true);
  });

  it('should set error when username is too long', () => {
    signUp.username = 'sdlkfdshfjaldsfurhioandkfldksjfhakjsdlhfljskdfdslfk';
    signUp.$v.username.$touch();
    expect(signUp.$v.username.$error).toBe(true);
  });

  it('should set error when age(birthdate) < 18', () => {
    signUp.birthdate = '2002-05-03';
    signUp.$v.birthdate.$touch();
    expect(signUp.$v.birthdate.$error).toBe(true);
  });

  it('should signUp when passwords match', async () => {
    mockedAxios.post.mockReturnValue(
      Promise.resolve({
        data: {},
        status: 201
      })
    );

    // set the values on component
    fillSignUpForm(signUp);

    signUp.$v.$touch();
    expect(signUp.$v.$invalid).toBe(false);
    await signUp.submit();

    expect(mockedAxios.post).toHaveBeenCalledWith('/api/v1/register', signUpAccount);
    expect(wrapper.vm.$route.path).toBe('/activate');
  });
});
