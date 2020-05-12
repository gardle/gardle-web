import { Component, Inject } from 'vue-property-decorator';
import Vue from 'vue';
import { validationMixin } from 'vuelidate';
import { email, helpers, maxLength, minLength, required, sameAs } from 'vuelidate/lib/validators';
import { EventBus } from '@/shared/eventbus/eventbus';
import AuthenticationService from '@/shared/services/auth/authentication.service';
import { SignUpUser } from '@/shared/model/signUpUser.model';
import { validationMessage } from 'vuelidate-messages';
import { acceptance, isOlderThan, username } from '@/shared/validations';

const telValid = helpers.regex('tel', /^[+]?[(]?[0-9]{0,4}[)]?[-\s.\/0-9]{4,128}$/);

const signUpFormValidations = {
  required({ $params }, props) {
    return this.$t(`register.messages.validation.${props.fieldName}.required`);
  },
  needsAccept(_, props) {
    return this.$t(`register.messages.validation.${props.fieldName}.needsAccept`);
  },
  email(_, props) {
    return this.$t(`register.messages.validation.${props.fieldName}.email`);
  },
  maxLength({ $params }, props) {
    return this.$t(`register.messages.validation.${props.fieldName}.maxLength`, [$params.maxLength.max]);
  },
  minLength({ $params }, props) {
    return this.$t(`register.messages.validation.${props.fieldName}.minLength`, [$params.minLength.min]);
  },
  isOlderThan({ $params }, props) {
    return this.$t(`register.messages.validation.${props.fieldName}.isOlderThan`, [$params.isOlderThan.minYears]);
  },
  sameAs({ $params }, props) {
    return this.$t(`register.messages.validation.${props.fieldName}.sameAs`);
  },
  numeric({ $params }, props) {
    return this.$t(`register.messages.validation.${props.fieldName}.numeric`);
  },
  telValid({ $params }, props) {
    return this.$t(`register.messages.validation.${props.fieldName}.invalidTel`);
  },
  username({ $params }, props) {
    return this.$t(`register.messages.validation.${props.fieldName}.username`);
  }
};

@Component({
  mixins: [validationMixin],
  validations: {
    firstname: { required },
    lastname: { required },
    email: { required, email },
    username: { required, maxLength: maxLength(50), username },
    tel: { required, telValid },
    birthdate: { required, isOlderThan: isOlderThan(18) },
    password: { required, minLength: minLength(12) },
    passwordConfirm: { required, sameAs: sameAs('password') },
    gtcAccept: { needsAccept: acceptance },
    iban: { required }
  }
})
export default class PageSignup extends Vue {
  firstname: string = null;
  lastname: string = null;
  email: string = null;
  username: string = null;
  tel: string = null;
  birthdate: string = null;
  password: string = null;
  eric = false;
  passwordConfirm: string = null;
  gtcAccept: boolean = null;
  iban: string = null;

  @Inject('authenticationService')
  authenticationService: () => AuthenticationService;

  mounted() {
    EventBus.$emit('update-navigation', null);
  }

  public handleHasAccount() {
    EventBus.$emit('show-login');
  }

  get validMessages() {
    return validationMessage(signUpFormValidations, { first: 1 });
  }

  get firstnameErrors() {
    return this.validMessages(this.$v.firstname, { fieldName: 'firstname' });
  }

  get lastnameErrors() {
    return this.validMessages(this.$v.lastname, { fieldName: 'lastname' });
  }

  get emailErrors() {
    return this.validMessages(this.$v.email, { fieldName: 'email' });
  }

  get usernameErrors() {
    return this.validMessages(this.$v.username, { fieldName: 'username' });
  }

  get telErrors() {
    return this.validMessages(this.$v.tel, { fieldName: 'tel' });
  }

  get birthdateErrors() {
    return this.validMessages(this.$v.birthdate, { fieldName: 'birthdate', minYears: 18 });
  }

  get passwordErrors() {
    return this.validMessages(this.$v.password, { fieldName: 'password' });
  }

  get passwordConfirmErrors() {
    return this.validMessages(this.$v.passwordConfirm, { fieldName: 'passwordConfirm' });
  }

  get gtcAcceptErrors() {
    return this.validMessages(this.$v.gtcAccept, { fieldName: 'gtcAccept' });
  }

  get ibanErrors() {
    return this.validMessages(this.$v.iban, { fieldName: 'iban' });
  }

  public async submit() {
    this.$v.$touch();

    if (!this.$v.$invalid) {
      const data = new SignUpUser(
        this.firstname,
        this.lastname,
        this.username,
        this.email,
        this.birthdate,
        this.tel,
        this.password,
        this.iban
      );

      try {
        const res = await this.authenticationService().register(data);

        this.$router.push({ path: '/activate' });
      } catch (err) {
        if (err instanceof Error) {
          let err_data = null;
          switch (err.name) {
            default:
              console.error(err);
              err_data = {
                title_key: 'UNKNOWN_ERROR'
              };
              break;
          }
          EventBus.$emit('add-notification', {
            text: this.$t('error.' + err_data.title_key),
            color: 'error',
            close: true,
            timeout: 6000
          });
        }
      }
    }
  }
}
