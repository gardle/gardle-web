// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.common with an alias.
import Vue from 'vue';
import App from '@/app.vue';
import router from '@/router';
import i18n from '@/shared/config/i18n';
import store from '@/store';
import * as config from '@/shared/config/config';
import * as vuetifyConfig from '@/shared/config/config-vuetify';
// currently not needed
// import '../content/scss/vendor.scss';
import TranslationService from '@/locale/translation.service';
import AuthenticationService from '@/shared/services/auth/authentication.service';
import ActivationService from '@/shared/services/auth/activation.service';
import GardenFieldService from '@/shared/services/gardenField/gardenfield.service';
import SearchService from '@/shared/services/gardenField/search/search.service';
import AccountService from '@/shared/services/auth/account.service';
import MessageService from '@/shared/services/Message/message.service';
import ImageCompressorService from '@/shared/services/util/image.compressor.service';
import LeasingService from '@/shared/services/leasing/leasing.service';
import PaymentService from '@/shared/services/payment.service';

// jhipster-needle-add-entity-service-to-main-import - JHipster will import entities services here

Vue.config.productionTip = false;
config.initVueApp(Vue);
const vuetify = vuetifyConfig.initVuetify(Vue);

const translationService = new TranslationService(store, i18n);

router.beforeEach((to, from, next) => {
  if (!to.matched.length) {
    next('/not-found');
  }

  if (to.meta && to.meta.needsAuth && !store.getters.loggedIn) {
    store.commit('userLogout');
  }

  next();
});

/* tslint:disable */
new Vue({
  components: { App },
  template: '<App/>',
  router,
  vuetify,
  provide: {
    translationService: () => translationService,
    authenticationService: () => new AuthenticationService(),
    activationService: () => new ActivationService(),
    gardenFieldService: () => new GardenFieldService(),
    imageCompressorService: () => new ImageCompressorService(),
    accountService: () => new AccountService(),
    leasingService: () => new LeasingService(),
    messageService: () => new MessageService(),
    searchService: () => new SearchService(),
    paymentService: () => new PaymentService()
    // jhipster-needle-add-entity-service-to-main - JHipster will import entities services here
  },
  i18n,
  store
}).$mount('#app');
