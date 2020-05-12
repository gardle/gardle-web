import axios from 'axios';
import VueI18n from 'vue-i18n';
import { Store } from 'vuex';

export default class TranslationService {
  private store: Store<{}>;
  private i18n: VueI18n;

  constructor(store: Store<{}>, i18n: VueI18n) {
    this.store = store;
    this.i18n = i18n;
    this.refreshTranslation();
  }

  public refreshTranslation(newLanguage?: string) {
    let currentLanguage = this.store.getters.currentLanguage;
    currentLanguage = newLanguage ? newLanguage : currentLanguage || 'de';
    if (this.i18n && !this.i18n.messages[currentLanguage]) {
      this.i18n.setLocaleMessage(currentLanguage, {});
      axios.get('i18n/' + currentLanguage + '.json').then(res => {
        if (res.data) {
          this.i18n.setLocaleMessage(currentLanguage, res.data);
          this.i18n.locale = currentLanguage;
          this.store.commit('updateLanguage', currentLanguage);
        }
      });
    } else if (this.i18n) {
      this.i18n.locale = currentLanguage;
      this.store.commit('updateLanguage', currentLanguage);
    }
  }

  public getLocale() {
    return this.i18n.locale;
  }
}
