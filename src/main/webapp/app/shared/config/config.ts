import PortalVue from 'portal-vue';
import { setupAxiosInterceptors } from '@/shared/config/axios-interceptor';

import VueCookie from 'vue-cookie';
import Vuelidate from 'vuelidate';
import Vue2Filters from 'vue2-filters';

import store from '@/store';
import router from '@/router';
import i18n from '@/shared/config/i18n';
import * as filters from '@/shared/filters';
import { EventBus } from '@/shared/eventbus/eventbus';
import { mapException } from '@/shared/exceptions';

export function initVueApp(vue) {
  vue.use(VueCookie);
  vue.use(Vuelidate);
  vue.use(Vue2Filters);
  vue.use(PortalVue);
  setupAxiosInterceptors(
    () => {
      store.commit('userLogout');
      EventBus.$emit('add-notification', {
        text: i18n.t('error.USER_GOT_UNAUTHENTICATED'),
        color: 'warning'
      });
      router.push('/');
    },
    err => {
      const exception = mapException(err);
      EventBus.$emit('add-notification', {
        text: i18n.t('error.' + exception.title_key),
        color: exception.level,
        primaryAction: {
          icon: exception.icon || null
        }
      });
    }
  );
  filters.initFilters();
}
