import Vue from 'vue';
import VueI18n from 'vue-i18n';

Vue.use(VueI18n);

const dateTimeFormats = {
  en: {
    short: {
      year: 'numeric',
      month: 'numeric',
      day: 'numeric'
    },
    timestamp: {
      year: 'numeric',
      month: 'numeric',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric'
    },
    long: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      weekday: 'short',
      hour: 'numeric',
      minute: 'numeric'
    }
  },
  de: {
    short: {
      year: 'numeric',
      month: 'numeric',
      day: 'numeric'
    },
    timestamp: {
      year: 'numeric',
      month: 'numeric',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric'
    },
    long: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      weekday: 'short',
      hour: 'numeric',
      minute: 'numeric',
      hour12: true
    }
  }
};

export default new VueI18n({
  silentTranslationWarn: true,
  dateTimeFormats
});
