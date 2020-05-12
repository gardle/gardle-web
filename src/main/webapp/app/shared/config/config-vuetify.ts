import '@mdi/font/css/materialdesignicons.css';
import Vuetify from 'vuetify';
import 'vuetify/dist/vuetify.min.css';

const configVuetify = () => {
  return new Vuetify({
    icons: {
      iconfont: 'mdi'
    }
  });
};

export function initVuetify(vue) {
  vue.use(Vuetify);

  return configVuetify();
}
