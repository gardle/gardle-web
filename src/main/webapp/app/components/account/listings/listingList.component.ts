import { Component, Prop } from 'vue-property-decorator';
import Vue from 'vue';
import ListingItem from '@/components/account/listings/listingItem.vue';

@Component({
  components: {
    ListingItem
  }
})
export default class ListingList extends Vue {
  @Prop({ type: Array, default: () => [] }) readonly listings: object[];
}
