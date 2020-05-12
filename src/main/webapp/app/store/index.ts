import Vue from 'vue';
import Vuex from 'vuex';
import { GARDLE_TOKEN_NAME } from '@/constants';
import VuexPersistedState from 'vuex-persistedstate';

const userStateTemplate = () => ({
  token: null,
  id: null,
  firstname: null,
  lastname: null
});

Vue.use(Vuex);

export const genDefaultState = () => ({
  user: Object.assign({}, userStateTemplate()),
  currentLanguage: null
});

export default new Vuex.Store({
  state: genDefaultState,
  mutations: {
    userLogin(state, { token, id, firstname, lastname }) {
      state.user.token = token;
      state.user.id = id;
      state.user.firstname = firstname;
      state.user.lastname = lastname;
      localStorage.setItem(GARDLE_TOKEN_NAME, token);
    },
    userUpdate(state, { firstname, lastname }) {
      state.user.firstname = firstname;
      state.user.lastname = lastname;
    },
    userLogout(state) {
      state.user = Object.assign({}, userStateTemplate());
      localStorage.removeItem(GARDLE_TOKEN_NAME);
    },
    updateLanguage(state, newLang) {
      state.currentLanguage = newLang;
    }
  },
  getters: {
    loggedIn: state => state.user.token !== null,
    userId: state => state.user.id,
    userName: state => `${state.user.firstname} ${state.user.lastname}`,
    currentLanguage: state => state.currentLanguage
  },
  plugins: process.env.NODE_ENV !== 'test' ? [VuexPersistedState({ key: 'gardle' })] : []
});
