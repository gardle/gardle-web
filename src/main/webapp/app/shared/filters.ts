import Vue from 'vue';
import i18n from '@/shared/config/i18n';
import { differenceInDays, parseISO } from 'date-fns/fp';
import { formatDistanceStrict, formatDistanceToNow } from 'date-fns';
import { de, enGB } from 'date-fns/locale';
import store from '@/store';

export function initFilters() {
  Vue.filter('formatDate', function(value, format) {
    if (value && format) {
      return i18n.d(parseISO(value), format);
    }
    return '';
  });

  Vue.filter('formatNumber', function(value, digits = 2) {
    if (value && digits) {
      return (Math.round(value * 100) / 100).toFixed(digits);
    }
    return '';
  });

  Vue.filter('calcPeriodPrice', function(value, from = null, to = null) {
    if (value && from && to) {
      return differenceInDays(parseISO(from), parseISO(to)) * value;
    }
    return '';
  });

  Vue.filter('humanizeDateDistance', function(value, locale = 'de') {
    if (value && locale) {
      const langKey = store.getters.currentLanguage || locale;
      const lang = langKey === 'de' ? de : enGB;
      return formatDistanceToNow(parseISO(value), {
        locale: lang
      });
    }
    return '';
  });

  Vue.filter('humanizeDateInterval', function(from, to, locale = 'de') {
    if (from && to && locale) {
      const langKey = store.getters.currentLanguage || locale;
      const lang = langKey === 'de' ? de : enGB;
      return formatDistanceStrict(parseISO(from), parseISO(to), {
        locale: lang
      });
    }
    return '';
  });
}
