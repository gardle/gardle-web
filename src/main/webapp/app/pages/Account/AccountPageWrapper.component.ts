import Component from 'vue-class-component';
import Vue from 'vue';
import { EventBus } from '@/shared/eventbus/eventbus';
import { Inject } from 'vue-property-decorator';
import AccountService from '@/shared/services/auth/account.service';
import { User } from '@/shared/model/user.model';

@Component
export default class AccountPageWrapper extends Vue {
  account: User = null;

  @Inject('accountService')
  accountService: () => AccountService;

  get userName() {
    return this.$store.getters.userName;
  }

  pages = [
    {
      icon: 'mdi-account',
      hasUserName: true,
      to: '/account/settings',
      twoLine: true,
      subTitle: 'settings.title',
      classes: 'grey lighten-3'
    },
    {
      icon: 'mdi-view-dashboard',
      key: 'dashboard.title',
      name: 'Dashboard',
      to: '/account/dashboard'
    },
    {
      icon: 'mdi-forum',
      key: 'message.home.title',
      name: 'Messages',
      to: '/account/messages'
    },
    {
      icon: 'mdi-map',
      key: 'leasing.title',
      name: 'Leasings',
      to: '/account/leasings'
    },
    {
      icon: 'mdi-flower',
      key: 'listing.title',
      name: 'Listings',
      to: '/account/listings'
    }
  ];

  page_active_class = 'green lighten-3 green--text text--darken-2';

  mounted() {
    if (this.$route.name === 'Account:Root') {
      // default redirect to dashboard
      this.$router.replace('/account/dashboard/');
    }

    EventBus.$emit('update-navigation', 'user-settings');
  }
}
