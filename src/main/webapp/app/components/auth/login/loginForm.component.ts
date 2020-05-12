import { Component, Inject } from 'vue-property-decorator';
import Vue from 'vue';
import { validationMixin } from 'vuelidate';
import { required } from 'vuelidate/lib/validators';
import { validationMessage } from 'vuelidate-messages';
import AuthenticationService from '@/shared/services/auth/authentication.service';
import { EventBus } from '@/shared/eventbus/eventbus';
import { username } from '@/shared/validations';

const loginFormValidations = {
  required({ $params }, props) {
    return this.$t(`login.messages.validation.${props.fieldName}.required`);
  },
  username({ $params }, props) {
    return this.$t(`login.messages.validation.${props.fieldName}.username`);
  }
};

@Component({
  mixins: [validationMixin],
  validations: {
    username: { required, username },
    password: { required }
  }
})
export default class LoginForm extends Vue {
  @Inject('authenticationService')
  authenticationService: () => AuthenticationService;

  username: string = null;
  password: string = null;

  created() {
    EventBus.$on('login-hidden', this.reset);
  }

  beforeDestroy() {
    EventBus.$off('login-hidden', this.reset);
  }

  get validMessages() {
    return validationMessage(loginFormValidations, { first: 1 });
  }

  get usernameErrors() {
    return this.validMessages(this.$v.username, { fieldName: 'username' });
  }

  get passwordErrors() {
    return this.validMessages(this.$v.password, { fieldName: 'password' });
  }

  public async submit() {
    this.$v.$touch();

    if (!this.$v.$invalid) {
      let res = null;
      try {
        res = await this.authenticationService().authenticate(this.username, this.password);
      } catch (e) {
        if (e instanceof Error) {
          let err_data = null;
          switch (e.name) {
            default:
              err_data = { title_key: 'UNKNOWN_ERROR' };
          }

          EventBus.$emit('add-notification', {
            text: this.$t('error.' + err_data.title_key),
            color: 'error',
            close: true,
            timeout: 6000
          });
        }
        return;
      }

      this.$store.commit('userLogin', {
        id: res.id,
        token: res.id_token,
        firstname: res.firstname,
        lastname: res.lastname
      });

      EventBus.$emit('hide-login');
      EventBus.$emit('add-notification', {
        text: this.$t('login.messages.success'),
        color: 'success',
        primaryAction: {
          icon: 'mdi-check'
        }
      });

      this.$router.push({ name: 'Account:Dashboard' });
    }
  }

  public reset() {
    this.$v.$reset();
    this.username = null;
    this.password = null;
  }
}
