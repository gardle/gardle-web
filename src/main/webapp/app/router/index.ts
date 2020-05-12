import Vue from 'vue';
import Component from 'vue-class-component';
import Router from 'vue-router';

Component.registerHooks([
  'beforeRouteEnter',
  'beforeRouteLeave',
  'beforeRouteUpdate' // for vue-router 2.2+
]);

const Wrapper = () => import('@/pages/Wrapper.vue');
const PageNotFound = () => import('@/pages/NotFound.vue');

const PageHome = () => import('@/pages/Home/Home.vue');
const PageSearchResults = () => import('@/pages/SearchResults/SearchResults.vue');
const PageListingDetail = () => import('@/pages/ListingDetail/ListingDetail.vue');

const PageSignUp = () => import('@/pages/auth/SignUp/SignUp.vue');
const PageActivate = () => import('@/pages/auth/Activate/Activate.vue');

const AccountWrapper = () => import('@/pages/Account/AccountPageWrapper.vue');
const PageDashboard = () => import('@/pages/Account/Dashboard/Dashboard.vue');
const PageSettings = () => import('@/pages/Account/Settings/Settings.vue');
const PageListings = () => import('@/pages/Account/Listings/Listings.vue');
const PageCreateListing = () => import('@/pages/Account/Listings/CreateListing.vue');
const PageMessages = () => import('@/pages/Account/Messages/Messages.vue');
const PageMessageThread = () => import('@/pages/Account/Messages/MessageThread.vue');
const PageLeasings = () => import('@/pages/Account/Leasings/Leasings.vue');

const PagePaymentEvent = () => import('@/pages/Payment/PaymentEvent.vue');
const PageAccountEvent = () => import('@/pages/Account/AccountEvent.vue');
/* tslint:disable */
// jhipster-needle-add-entity-to-router-import - JHipster will import entities to the router here

Vue.use(Router);

// prettier-ignore
export default new Router({
  mode: 'history',
  routes: [
    {
      path: '/',
      name: 'Home',
      component: PageHome
    },
    {
      path: '/signup',
      name: 'SignUp',
      component: PageSignUp
    },
    {
      path: '/activate',
      name: 'Activate',
      component: PageActivate
    },
    {
      path: '/search',
      name: 'SearchResults',
      component: PageSearchResults
    },
    {
      path: '/fields/:id',
      name: 'ListingDetail',
      component: PageListingDetail,
        props: (route) => ({'fieldId': route.params.id})
    },
    {
      path: '/account',
      component: AccountWrapper,
      meta: {
        needsAuth: true
      },
      children: [
        {
          path: '',
          name: 'Account:Root',
          component: PageDashboard
        },
        {
          path: 'dashboard',
          name: 'Account:Dashboard',
          component: PageDashboard
        },
        {
          path: 'settings',
          name: 'Account:Settings',
          component: PageSettings
        },
        {
          path: 'verification',
          component: Wrapper,
          children: [
            {
              path: 'success',
              component: PageAccountEvent
            },
            {
              path: 'error',
              component: PageAccountEvent
            }
          ]
        },
        {
          path: 'listings',
          component: Wrapper,
          children: [
            {
              path: '',
              name: 'Account:Listings',
              component: PageListings
            },
            {
              path: 'create',
              name: 'Account:Listings:Create',
              component: PageCreateListing
            },
            {
              path: 'edit',
              name: 'Account:Listings:Edit',
              component: PageCreateListing,
              props: true
            }
          ]
        },
        {
          path: 'leasings',
          name: 'Account:Leasings',
          component: PageLeasings
        },
        {
          path: 'messages',
          component: Wrapper,
          children: [
            {
              path: '',
              name: 'Account:Messages',
              component: PageMessages,
            },
            {
              path: 'thread/:id',
              name: 'Account:Messages:Thread',
              component: PageMessageThread
            },
          ]
        }
      ]
    },
    {
      path: '/payment',
      component: Wrapper,
      children: [
        {
          path: 'success',
          component: PagePaymentEvent
        },
        {
          path: 'error',
          component: PagePaymentEvent
        }
      ]
    },
    {
      path: '*',
      name: 'page-not-found',
      component: PageNotFound
    }
    // jhipster-needle-add-entity-to-router - JHipster will add entities to the router here
  ]
});
