import Vue from 'vue';
import Component from 'vue-class-component';
import LoginForm from '@/components/auth/login/loginForm.vue';
import { EventBus } from '@/shared/eventbus/eventbus';
import { snackbarQueue } from '@/mixins/snackbarQueue';
import { Inject, Watch } from 'vue-property-decorator';
import notificationTray from '@/components/Messages/notificationTray.vue';
import TranslationService from '@/locale/translation.service';

@Component({
  mixins: [snackbarQueue],
  components: {
    LoginForm,
    notificationTray
  }
})
export default class App extends Vue {
  drawer = null;
  loginModal = false;
  mainNavigation = null;

  @Inject('translationService')
  translationService: () => TranslationService;

  languages = ['de', 'en'];

  changeLanguage(lang) {
    this.translationService().refreshTranslation(lang);
  }

  get currentLang() {
    return this.$store.getters.currentLanguage;
  }

  @Watch('loginModal', { immediate: true })
  loginModalChanged(val: boolean, oldVal: boolean) {
    if (val === false) {
      EventBus.$emit('login-hidden');
    }
  }

  mounted() {
    EventBus.$on('update-navigation', this.handleUpdateNavigation);
    EventBus.$on('show-login', this.handleLoginShow);
    EventBus.$on('hide-login', this.handleLoginHide);
  }

  beforeDestroy() {
    EventBus.$off('update-navigation', this.handleUpdateNavigation);
    EventBus.$off('show-login', this.handleLoginShow);
    EventBus.$off('hide-login', this.handleLoginHide);
  }

  get appBarClasses() {
    return this.$route.name !== 'Home' ? 'border-bottom: 1px solid #dfdfdf !important' : '';
  }

  get hasMainNavigation() {
    return this.mainNavigation !== null;
  }

  get loggedIn() {
    return this.$store.getters.loggedIn;
  }

  public handleUpdateNavigation(content) {
    this.mainNavigation = content;
  }

  public handleLoginShow() {
    this.loginModal = true;
  }

  public handleLoginHide() {
    this.loginModal = false;
  }

  public handleSignUpClick() {
    this.$router.push('/signup');
  }

  public handleLogoutClick() {
    this.$store.commit('userLogout');
    this.$router.push('/');
    EventBus.$emit('add-notification', {
      text: this.$t('logout.messages.success'),
      color: 'info',
      primaryAction: {
        icon: 'mdi-logout'
      }
    });
  }
}
