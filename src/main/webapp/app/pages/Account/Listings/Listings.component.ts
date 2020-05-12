import { Component, Inject } from 'vue-property-decorator';
import Vue from 'vue';
import ListingList from '@/components/account/listings/listingList.vue';
import GardenFieldService from '@/shared/services/gardenField/gardenfield.service';
import PaymentService from '@/shared/services/payment.service';
import AccountService from '@/shared/services/auth/account.service';

@Component({
  components: {
    ListingList
  }
})
export default class PageListings extends Vue {
  loading = false;

  listings: any[] = [];

  paymentAccountVerified = null;

  @Inject('gardenFieldService')
  gardenFieldService: () => GardenFieldService;

  @Inject('paymentService')
  paymentService: () => PaymentService;

  @Inject('accountService')
  accountService: () => AccountService;

  mounted() {
    this.getAccountIsVerified().then(res => {
      if (res === true) {
        this.getUserGardenfields();
      }
      this.paymentAccountVerified = res;
    });
  }

  public getAccountIsVerified() {
    return this.accountService()
      .getAccount()
      .then(res => {
        return res.stripeAccountVerified === true;
      });
  }

  public async getUserGardenfields() {
    this.loading = true;
    try {
      const fields = await this.gardenFieldService().getUserGardenfields();
      this.listings = fields;
    } catch (e) {
      console.error(e);
    } finally {
      this.loading = false;
    }
  }

  public handleCreateListingClick() {
    this.$router.push({ name: 'Account:Listings:Create' });
  }

  public handleEditListing(listing) {
    this.$router.push({
      name: 'Account:Listings:Edit',
      params: {
        editItem: listing
      }
    });
  }

  public handleDetailsListing(listingId) {
    this.$router.push({
      name: 'ListingDetail',
      params: {
        id: listingId
      }
    });
  }

  public async handleDeleteListing(id) {
    try {
      const deleteListing = this.listings.filter(listing => listing.id === id)[0];

      await this.gardenFieldService().delete(id);

      this.listings.splice(this.listings.indexOf(deleteListing), 1);
    } catch (e) {
      console.error(e);
    }
  }

  public async handleGetAccountLinks() {
    // call payment service for account links url
    // get the URL open in a new tab
    // *user enters his payment information to Stripe*
    this.paymentService()
      .getAccountSetupURL()
      .then(res => {
        window.location = res;
      })
      .catch(err => {
        console.error(err);
      });
  }
}
