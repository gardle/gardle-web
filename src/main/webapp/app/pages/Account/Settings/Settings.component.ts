import { Component, Inject } from 'vue-property-decorator';
import Vue from 'vue';
import { email, helpers, maxLength, minLength, required, sameAs } from 'vuelidate/lib/validators';
import AccountService from '@/shared/services/auth/account.service';
import { User } from '@/shared/model/user.model';
import { EventBus } from '@/shared/eventbus/eventbus';
import { validationMixin } from 'vuelidate';
import { validationMessage } from 'vuelidate-messages';
import { isOlderThan } from '@/shared/validations';
import TranslationService from '@/locale/translation.service';

const telValid = helpers.regex('tel', /^[+]?[(]?[0-9]{0,4}[)]?[-\s.\/0-9]{4,128}$/);

const settingsValidations = {
  required({ $params }, props) {
    return this.$t(`settings.messages.validation.${props.fieldName}.required`);
  },
  email(_, props) {
    return this.$t(`settings.messages.validation.${props.fieldName}.email`);
  },
  maxLength({ $params }, props) {
    return this.$t(`settings.messages.validation.${props.fieldName}.maxLength`, [$params.maxLength.max]);
  },
  minLength({ $params }, props) {
    return this.$t(`settings.messages.validation.${props.fieldName}.minLength`, [$params.minLength.min]);
  },
  isOlderThan({ $params }, props) {
    return this.$t(`settings.messages.validation.${props.fieldName}.isOlderThan`, [$params.isOlderThan.minYears]);
  },
  sameAs({ $params }, props) {
    return this.$t(`settings.messages.validation.${props.fieldName}.sameAs`);
  },
  telValid({ $params }, props) {
    return this.$t(`settings.messages.validation.${props.fieldName}.invalidTel`);
  },
  username({ $params }, props) {
    return this.$t(`settings.messages.validation.${props.fieldName}.username`);
  }
};

const generatePasswordChangeFields = () => ({
  currentPassword: null,
  newPassword: null,
  repeatPassword: null
});

@Component({
  mixins: [validationMixin],
  validations: {
    account: {
      firstName: {
        required,
        minLength: minLength(1),
        maxLength: maxLength(50)
      },
      lastName: {
        required,
        minLength: minLength(1),
        maxLength: maxLength(50)
      },
      email: {
        required,
        email,
        minLength: minLength(5),
        maxLength: maxLength(254)
      },
      birthDate: {
        required,
        isOlderThan: isOlderThan(18)
      },
      tel: {
        required,
        telValid
      }
    },
    passwordChange: {
      currentPassword: {
        required
      },
      newPassword: {
        required,
        minLength: minLength(12)
      },
      repeatPassword: {
        required,
        minLength: minLength(12),
        sameAs: sameAs('newPassword')
      }
    }
  }
})
export default class PageSettings extends Vue {
  languageKeys = ['en', 'de'];
  account: User = null;

  passwordChange = generatePasswordChangeFields();

  @Inject('accountService')
  accountService: () => AccountService;

  @Inject('translationService')
  translationService: () => TranslationService;

  fetchAccount() {
    this.accountService()
      .getAccount()
      .then(account => {
        this.account = account;
        this.$store.commit('userUpdate', {
          firstname: account.firstName,
          lastname: account.lastName
        });
        this.$v.account.$touch();
      })
      .catch(response => {
        console.error(response);
        EventBus.$emit('add-notification', {
          text: this.$t('settings.notifications.errorFetchingData'),
          color: 'error',
          close: true,
          timeout: 6000
        });
      });
  }

  created() {
    this.fetchAccount();
  }

  saveAccountData() {
    this.accountService()
      .save(this.account)
      .then(response => {
        console.log(response);
        this.fetchAccount();

        if (this.account.langKey !== this.$store.getters.currentLanguage) {
          this.translationService().refreshTranslation(this.account.langKey);
        }

        EventBus.$emit('add-notification', {
          text: this.$t('settings.notifications.accountUpdated'),
          color: 'success',
          close: true,
          timeout: 6000
        });
      })
      .catch(err => {
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
      });
  }

  changePassword() {
    this.accountService()
      .changePassword(this.passwordChange.currentPassword, this.passwordChange.newPassword)
      .then(response => {
        this.passwordChange = generatePasswordChangeFields();
        this.$v.passwordChange.$reset();

        EventBus.$emit('add-notification', {
          text: this.$t('settings.notifications.passwordChanged'),
          color: 'success',
          close: true,
          timeout: 6000
        });
      })
      .catch(err => {
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
      });
  }

  get validMessages() {
    return validationMessage(settingsValidations, { first: 1 });
  }

  get firstnameErrors() {
    return this.validMessages(this.$v.account.firstName, { fieldName: 'firstname' });
  }

  get lastnameErrors() {
    return this.validMessages(this.$v.account.lastName, { fieldName: 'lastname' });
  }

  get emailErrors() {
    return this.validMessages(this.$v.account.email, { fieldName: 'email' });
  }

  get birthDateErrors() {
    return this.validMessages(this.$v.account.birthDate, { fieldName: 'birthDate' });
  }

  get telErrors() {
    return this.validMessages(this.$v.account.tel, { fieldName: 'tel' });
  }

  get loginErrors() {
    return this.validMessages(this.$v.account.login, { fieldName: 'login' });
  }

  get currentPasswordErrors() {
    return this.validMessages(this.$v.passwordChange.currentPassword, { fieldName: 'currentPassword' });
  }

  get newPasswordErrors() {
    return this.validMessages(this.$v.passwordChange.newPassword, { fieldName: 'newPassword' });
  }

  get repeatPasswordErrors() {
    return this.validMessages(this.$v.passwordChange.repeatPassword, { fieldName: 'repeatPassword' });
  }
}
