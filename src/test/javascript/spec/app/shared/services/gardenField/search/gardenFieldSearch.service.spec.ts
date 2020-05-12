import axios from 'axios';
import SearchService from '@/shared/services/gardenField/search/search.service';
import { GardenFieldFilterCriteria, IGardenFieldFilterCriteria } from '@/shared/model/gardenFieldFilterCriteria.model';
import { PageModel } from '@/shared/model/page.model';

const mockedAxios: any = axios;

jest.mock('axios', () => ({
  get: jest.fn()
}));

describe('Gardenfield search service', () => {
  let searchService: SearchService;

  beforeEach(() => {
    mockedAxios.get.mockReset();
    searchService = new SearchService();
  });

  /* TODO it('invalid number filter criteria: should throw GardenFieldFilterValidationError', () => {
     const gardenFieldFilterCriteria: IGardenFieldFilterCriteria = new GardenFieldFilterCriteria(
       null,
       -1,
       -1,
       -1,
       -1,
       null,
       null,
       null,
       null
     );
     mockedAxios.get.mockReturnValue(
       Promise.reject({
         response: {
           status: 404,
           data: 'invalid filter criteria'
         }
       })
     );
     const filterCall = async () => {
       return await searchService.filter(gardenFieldFilterCriteria, new PageModel(0, 10));
     };

     // expect(filterCall()).rejects;
   }); */

  it('valid filters: should filter correctly', async () => {
    const gardenFieldFilterCriteria: IGardenFieldFilterCriteria = new GardenFieldFilterCriteria(
      null,
      1,
      10,
      15,
      100,
      null,
      null,
      null,
      null
    );
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        data: [],
        status: 200
      })
    );

    const filterCall = await searchService.filter(gardenFieldFilterCriteria, new PageModel(0, 10));

    expect(mockedAxios.get).toBeCalledWith(
      'api/v1/gardenfields/filter?page=0&size=10&minPrice=1&maxPrice=10&sizeInM2LowerBound=15&sizeInM2UpperBound=100'
    );
  });

  it('no filters: should filter correctly', async () => {
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        data: [],
        status: 200
      })
    );

    const res = await searchService.filter(new GardenFieldFilterCriteria(), new PageModel(0, 10));

    expect(mockedAxios.get).toBeCalledWith('api/v1/gardenfields/filter?page=0&size=10&');
  });

  it('valid autocomplete: should autocomplete correctly', async () => {
    const searchString = 'test search string';
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        data: [],
        status: 200
      })
    );

    const autocompleteCall = await searchService.autocomplete(searchString);

    expect(mockedAxios.get).toBeCalledWith('api/v1/gardenfields/autocomplete?partialSearchString=' + searchString);
  });

  /* TODO it('error in autocomplete: should reject autocomplete', async () => {
    const searchString: string = null;
    mockedAxios.get.mockReturnValue(
      Promise.reject({
        data: [],
        status: 500
      })
    );

    const call = async () => {
      return await searchService.autocomplete(searchString);
    };

    // expect(call()).rejects;
  }); */

  /* TODO it('wrong request in autocomplete: should reject autocomplete', async () => {
    const searchString: string = null;
    mockedAxios.get.mockReturnValue(
      Promise.reject({
        data: [],
        status: 400
      })
    );

    const call = async () => {
      return await searchService.autocomplete(searchString);
    };

    // expect(call()).rejects;
  }); */

  it('valid search: should filter for keywords correctly', async () => {
    const searchString = 'test search string';
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        data: [],
        status: 200
      })
    );

    await searchService.filter(
      new GardenFieldFilterCriteria(null, null, null, null, null, null, null, null, null, null, null, null, searchString),
      new PageModel(0, 10)
    );

    expect(mockedAxios.get).toBeCalledWith('api/v1/gardenfields/filter?page=0&size=10&keywords=test search string');
  });

  it('get all gardenfields: should return gfs correctly', async () => {
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        data: [],
        status: 200
      })
    );

    await searchService.getGardenFields(new PageModel(0, 10));

    expect(mockedAxios.get).toBeCalledWith('/api/v1/gardenfields?page=0&size=10');
  });

  /* TODO it('get all gardenfields error: should reject', async () => {
    mockedAxios.get.mockReturnValue(
      Promise.reject({
        data: [],
        status: 500
      })
    );

    const call = async () => {
      return await searchService.getGardenFields(new PageModel(0, 10));
    };

    // expect(call()).rejects;
  }); */

  /* TODO it('get all gardenfields error: should reject', async () => {
    mockedAxios.get.mockReturnValue(
      Promise.reject({
        data: [],
        status: 400
      })
    );

    const call = async () => {
      return await searchService.getGardenFields(new PageModel(0, 10));
    };

    // expect(call()).rejects;
  }); */
});
