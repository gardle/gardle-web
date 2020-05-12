import Vue from 'vue';
import { Component, Inject, Watch } from 'vue-property-decorator';
import { EventBus } from '@/shared/eventbus/eventbus';
import SearchService from '@/shared/services/gardenField/search/search.service';
import { IGardenField } from '@/shared/model/gardenfield.model';

@Component
export default class PageHome extends Vue {
  @Inject('searchService')
  searchService: () => SearchService;

  gardenFieldSearchTimer = null;
  searchVal: string = null;

  gardenFieldItems: IGardenField[] = [];
  selectedGardenField: IGardenField = null;

  isGardenFieldSearchLoading = false;

  mounted() {
    EventBus.$emit('update-navigation', null);
    this.setRandomBackground();
  }

  private setRandomBackground() {
    const backgrounds = ['candidate1', 'candidate3_crop', 'candidate5'];
    const bgrClass = document.querySelectorAll('.garden-background');
    const index = Math.floor(Math.random() * backgrounds.length);
    for (const elem of bgrClass as any) {
      elem.style.backgroundImage = 'url(/content/images/' + backgrounds[index] + '.jpg)';
    }
  }

  public scrollToPage(count) {
    this.$vuetify.goTo(window.innerHeight * count);
  }

  public handleExploreSearch() {
    this.$router.push({ path: '/search' });
  }

  public handleSearch() {
    this.$router.push({ path: `/search?keywords=${this.searchVal}` });
  }

  public async searchGardenField() {
    this.isGardenFieldSearchLoading = true;
    try {
      this.gardenFieldItems = await this.searchService().autocomplete(this.searchVal);
    } catch (error) {
      console.log('error getting autocomplete results: ' + error);
    }
    this.isGardenFieldSearchLoading = false;
  }

  public gardenFieldItem(item) {
    return item ? `${item.name}, ${item.city}` : null;
  }

  @Watch('searchVal', { immediate: true })
  private debounceSearch(e) {
    if (!e || e.length <= 3) {
      return;
    }
    // cancel pending call
    clearTimeout(this.gardenFieldSearchTimer);

    // delay new call 500ms
    this.gardenFieldSearchTimer = setTimeout(() => {
      this.searchGardenField();
    }, 500);
  }

  @Watch('selectedGardenField', { immediate: true })
  private showDetail(e) {
    if (e !== null) {
      this.$router.push(`/fields/${e.id}`);
    }
  }
}
