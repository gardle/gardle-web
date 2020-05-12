import axios from 'axios';
import GardenFieldService from '@/shared/services/gardenField/gardenfield.service';
import { IGardenField } from '@/shared/model/gardenfield.model';
import { CreateListing } from '@/shared/model/listing.model';

const mockedAxios: any = axios;

let createListing = new CreateListing();
createListing = {
  ...createListing,
  name: 'Test field',
  sizeInM2: 150.2,
  pricePerM2: 0.45,
  city: 'TestCity',
  latitude: 15.23158,
  longitude: 11.2315
};

const authRejectionResponse = {
  status: 401
};

describe('Gardenfield service', () => {
  let gardenService: GardenFieldService;
  beforeEach(() => {
    mockedAxios.get.mockReset();
    mockedAxios.post.mockReset();
    mockedAxios.delete.mockReset();

    gardenService = new GardenFieldService();
  });

  /* TODO it('get: should throw GardenFieldNotFoundError', () => {
    mockedAxios.get.mockReturnValue(
      Promise.reject({
        response: {
          status: 404
        }
      })
    );

    const getGardenField = async () => {
      return await gardenService.getGardenField(1);
    };

    expect(getGardenField());
  });*/

  it('get: should get gardenfield data correctly', async () => {
    const gardenFieldData: IGardenField = {
      id: 1,
      name: 'some sunny gardenfield',
      description: 'some description',
      sizeInM2: 132.7,
      pricePerM2: 2.47,
      latitude: 48.1935896,
      longitude: 16.3656036,
      city: 'Vienna',
      roofed: true,
      glassHouse: false,
      high: true,
      water: true,
      electricity: true,
      phValue: 6.2,
      owner: {
        id: 51,
        login: 'maxi1997',
        firstName: 'max',
        lastName: 'musterman',
        email: 'max1997@gmail.com'
      }
    };

    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        data: gardenFieldData
      })
    );

    const getGardenField = await gardenService.getGardenField(gardenFieldData.id);

    expect(mockedAxios.get).toBeCalledWith('/api/v1/gardenfields/' + gardenFieldData.id);
    expect(getGardenField).toBe(gardenFieldData);
  });

  /* TODO it('post: should create a gardenfield and return correct errors', async () => {
    mockedAxios.post.mockReturnValue(
      Promise.resolve({
        data: {
          ...createListing,
          id: 1
        }
      })
    );

    const createdListing = await gardenService.create(createListing);

    expect(mockedAxios.post).toBeCalledWith('/api/v1/gardenfields/', createListing);
    expect(createdListing).toHaveProperty('id', 1);

    mockedAxios.post.mockReturnValue(
      Promise.reject({
        response: authRejectionResponse
      })
    );

    const wrapper = async () => {
      return await gardenService.create(createListing);
    };

    await expect(wrapper()).rejects.toEqual(authRejectionResponse);

    mockedAxios.post.mockReturnValue(
      Promise.reject({
        response: {
          status: 400
        }
      })
    );

    await expect(wrapper());
  });*/

  /* TODO it('put: should update a gardenfield and return correct errors', async () => {
    const updateListing = {
      ...createListing,
      id: 1,
      name: 'Updated title'
    };

    mockedAxios.put.mockReturnValue(
      Promise.resolve({
        data: updateListing
      })
    );

    const updatedListing = await gardenService.update(updateListing);

    expect(mockedAxios.put).toBeCalledWith('/api/v1/gardenfields/', updateListing);
    expect(updatedListing).toHaveProperty('name', 'Updated title');

    mockedAxios.put.mockReturnValue(
      Promise.reject({
        response: authRejectionResponse
      })
    );

    const wrapper = async () => {
      return await gardenService.update(updateListing);
    };

    await expect(wrapper()).rejects.toEqual(authRejectionResponse);

    mockedAxios.put.mockReturnValue(
      Promise.reject({
        response: {
          status: 400
        }
      })
    );

    await expect(wrapper());
  });*/

  it('delete: should delete a gardenfield and return correct errors', async () => {
    const deleteId = 1;
    mockedAxios.delete.mockReturnValue(
      Promise.resolve({
        data: {}
      })
    );

    const wrapper = async () => {
      return await gardenService.delete(deleteId);
    };

    await expect(wrapper()).resolves.toEqual({});
    expect(mockedAxios.delete).toBeCalledWith(`/api/v1/gardenfields/${deleteId}`);

    mockedAxios.delete.mockReturnValue(
      Promise.reject({
        response: authRejectionResponse
      })
    );

    await expect(wrapper()).rejects.toEqual(authRejectionResponse);
    expect(mockedAxios.delete).toBeCalledWith(`/api/v1/gardenfields/${deleteId}`);
  });

  /* TODO it('should upload an image and return correct errors', async () => {
    const formData = new FormData();
    const fieldId = 1;

    mockedAxios.post.mockReturnValue(Promise.resolve({data: {}}));

    const wrapper = async () => {
      return await gardenService.uploadImage(fieldId, formData);
    };

    await expect(wrapper()).resolves.toEqual({});
    expect(mockedAxios.post).toBeCalledWith(`/api/v1/gardenfields/${fieldId}/uploadImage`, formData);

    mockedAxios.post.mockReturnValue(
      Promise.reject({
        response: authRejectionResponse
      })
    );

    await expect(wrapper()).rejects.toEqual(authRejectionResponse);

    mockedAxios.post.mockReturnValue(
      Promise.reject({
        response: {
          status: 400
        }
      })
    );

    await expect(wrapper());

    mockedAxios.post.mockReturnValue(
      Promise.reject({
        response: {
          status: 409
        }
      })
    );

    await expect(wrapper());
  });
 */

  /* TODO it('should delete an image and return correct errors', async () => {
    const imageName = '21363-a213-123a-sa31.png';
    const fieldId = 1;

    mockedAxios.delete.mockReturnValue(Promise.resolve({ data: {} }));

    const wrapper = async () => {
      return await gardenService.deleteImage(fieldId, imageName);
    };

    await expect(wrapper()).resolves.toEqual(undefined);
    expect(mockedAxios.delete).toBeCalledWith(`/api/v1/gardenfields/${fieldId}/${imageName}`);

    mockedAxios.delete.mockReturnValue(
      Promise.reject({
        response: authRejectionResponse
      })
    );

    await expect(wrapper()).rejects.toEqual(authRejectionResponse);

    mockedAxios.delete.mockReturnValue(
      Promise.reject({
        response: {
          status: 400
        }
      })
    );

    await expect(wrapper());

    mockedAxios.delete.mockReturnValue(
      Promise.reject({
        response: {
          status: 409
        }
      })
    );

    await expect(wrapper());
  }); */

  it('getGardenFieldCover: should return coverImageName correctly', async () => {
    const gardenFieldId = 1;
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        data: [],
        status: 200
      })
    );

    await gardenService.getGardenFieldCover(1);

    expect(mockedAxios.get).toBeCalledWith('/api/v1/gardenfields/' + gardenFieldId + '/coverImageName');
  });
});
