import axios from 'axios';
import { PageModel } from '@/shared/model/page.model';
import { IGardenFieldFilterCriteria } from '@/shared/model/gardenFieldFilterCriteria.model';
import { GardenFieldPage } from '@/shared/model/gardenFieldPage.model';
import { IGardenField } from '@/shared/model/gardenfield.model';
import { IGardenFieldFilterBoundaries } from '@/shared/model/gardenFieldFilterBoundaries.model';

const gardenFieldSearchApiClient = {
  getAllGardenFields(page: PageModel) {
    return axios.get(`/api/v1/gardenfields?page=${page.pageNumber}&size=${page.pageSize}`);
  },
  autocomplete(partialSearchString: string) {
    return axios.get(`api/v1/gardenfields/autocomplete?partialSearchString=${partialSearchString}`);
  },
  filter(page: PageModel, gardenFieldFilterCriteriaString: string) {
    return axios.get(`api/v1/gardenfields/filter?page=${page.pageNumber}&size=${page.pageSize}&${gardenFieldFilterCriteriaString}`);
  },
  filterBoundaries() {
    return axios.get('/api/v1/gardenfields/filterBoundaries');
  }
};

export default class SearchService {
  public autocomplete(partialSearchString: string): Promise<IGardenField[]> {
    return new Promise((resolve, reject) => {
      gardenFieldSearchApiClient
        .autocomplete(partialSearchString)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public getGardenFields(page: PageModel): Promise<GardenFieldPage> {
    return new Promise((resolve, reject) => {
      gardenFieldSearchApiClient
        .getAllGardenFields(page)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public async filter(gardenFieldFilterCriteria: IGardenFieldFilterCriteria, page: PageModel): Promise<GardenFieldPage> {
    let filterString = '';
    if (gardenFieldFilterCriteria !== null && gardenFieldFilterCriteria !== undefined) {
      filterString = gardenFieldFilterCriteria.getAsFilterString();
    }
    return new Promise((resolve, reject) => {
      gardenFieldSearchApiClient
        .filter(page, filterString)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public async getFilterBoundaries(): Promise<IGardenFieldFilterBoundaries> {
    return new Promise((resolve, reject) => {
      gardenFieldSearchApiClient
        .filterBoundaries()
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }
}
