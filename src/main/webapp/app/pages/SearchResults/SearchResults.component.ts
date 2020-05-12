import Vue from 'vue';
import { Component, Inject } from 'vue-property-decorator';
import FilterBar from '@/components/filter/FilterBar.vue';
import { EventBus } from '@/shared/eventbus/eventbus';
import { IGardenFieldFilterCriteria } from '@/shared/model/gardenFieldFilterCriteria.model';
import { GardenFieldPage } from '@/shared/model/gardenFieldPage.model';
import { PageModel } from '@/shared/model/page.model';
import SearchService from '@/shared/services/gardenField/search/search.service';
import GardenFieldService from '@/shared/services/gardenField/gardenfield.service';

@Component({
  components: {
    FilterBar
  }
})
export default class PageSearchResults extends Vue {
  pageSize = 10;
  loading = false;

  gardenFieldPage: GardenFieldPage = new GardenFieldPage(new PageModel(0, this.pageSize));

  filters: IGardenFieldFilterCriteria = null;

  @Inject('searchService')
  private searchService: () => SearchService;

  @Inject('gardenFieldService')
  private gardenFieldService: () => GardenFieldService;

  mounted() {
    EventBus.$emit('update-navigation', 'filter-results');
  }

  get isLoading() {
    return this.loading;
  }

  get hasResults() {
    return this.gardenFieldPage.content.length > 0;
  }

  public async loadResults() {
    this.loading = true;
    await this.getFilteredGardenFields();
    this.$forceUpdate();
    this.loading = false;
  }

  public handleFilterUpdate(filters: IGardenFieldFilterCriteria) {
    this.filters = filters;
    this.loadResults();
  }

  public handleResetFilters(): void {
    this.filters = null;
    this.handleUpdatePageNumber(1);
  }

  public handleUpdatePageNumber(pageNumber) {
    if (pageNumber - 1 === this.gardenFieldPage.pageable.pageNumber) {
      return;
    }
    this.gardenFieldPage.pageable.pageNumber = pageNumber - 1;
    this.loadResults();
  }

  public handleShowGardenFieldDetails(id): void {
    this.showListingDetail(id);
  }

  private async getFilteredGardenFields() {
    try {
      this.gardenFieldPage = await this.searchService().filter(
        this.filters,
        new PageModel(this.gardenFieldPage.pageable.pageNumber, this.gardenFieldPage.pageable.pageSize)
      );
      await this.loadCoverImages();
      if (this.gardenFieldPage.content.length === 0) {
        EventBus.$emit('add-notification', {
          text: this.$t('searchResult.notification.noResult'),
          color: 'info',
          close: true,
          timeout: 2500
        });
      }
    } catch (e) {
      EventBus.$emit('add-notification', {
        text: e,
        color: 'error',
        close: true,
        timeout: 6000
      });
    }
  }

  private async loadCoverImages() {
    const imagePromises = [];
    for (let i = 0; i < this.gardenFieldPage.content.length; i++) {
      imagePromises.push(this.getFieldCoverImagePromise(this.gardenFieldPage.content[i]));
    }
    await Promise.all(imagePromises);
  }

  private async getFieldCoverImagePromise(gardenfield): Promise<any> {
    return new Promise<string>((resolve, reject) => {
      this.gardenFieldService()
        .getGardenFieldCover(gardenfield.id)
        .then(coverImageUrl => {
          gardenfield.imageUrl = coverImageUrl;
          resolve(coverImageUrl);
        })
        .catch(resolve); // ignore if cover error happened
    });
  }

  public showListingDetail(gf) {
    this.$router.push(`/fields/${gf}`);
  }
}
