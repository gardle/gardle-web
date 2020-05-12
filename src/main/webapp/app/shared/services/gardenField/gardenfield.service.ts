import axios from 'axios';

import { ICreateListing } from '@/shared/model/listing.model';
import { IGardenField } from '@/shared/model/gardenfield.model';
import { IMAGE_BASE_URL } from '@/constants';

const gardenFieldClient = {
  getUserGardenFields() {
    return axios.get('/api/v1/gardenfields/user');
  },
  create(data) {
    return axios.post('/api/v1/gardenfields/', data);
  },
  update(id, data) {
    return axios.put(`/api/v1/gardenfields/`, data);
  },
  delete(id) {
    return axios.delete(`/api/v1/gardenfields/${id}`);
  },
  getGardenField(id: number) {
    return axios.get(`/api/v1/gardenfields/${id}`);
  },
  getImageNames(id: number) {
    return axios.get(`/api/v1/gardenfields/${id}/downloadImages`);
  },
  getCoverImageName(id: number) {
    return axios.get(`/api/v1/gardenfields/${id}/coverImageName`);
  },
  uploadImage(id, image) {
    return axios.post(`/api/v1/gardenfields/${id}/uploadImage`, image);
  },
  deleteImage(id, imagename) {
    return axios.delete(`/api/v1/gardenfields/${id}/${imagename}`);
  }
};

export default class GardenFieldService {
  public getUserGardenfields() {
    return new Promise<any>((resolve, reject) => {
      gardenFieldClient
        .getUserGardenFields()
        .then(res => {
          resolve(res.data.content);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public create(data: ICreateListing) {
    return new Promise<any>((resolve, reject) => {
      gardenFieldClient
        .create(data)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public update(data: any) {
    return new Promise<any>((resolve, reject) => {
      gardenFieldClient
        .update(data.id, data)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public delete(id: number) {
    return new Promise<any>((resolve, reject) => {
      gardenFieldClient
        .delete(id)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public uploadImage(id: number, image: FormData) {
    return new Promise<any>((resolve, reject) => {
      gardenFieldClient
        .uploadImage(id, image)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public deleteImage(id: number, image: string) {
    return new Promise<any>((resolve, reject) => {
      gardenFieldClient
        .deleteImage(id, image)
        .then(res => {
          resolve();
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  /**
   * Gets the gardenfield data from the backend
   *
   * @param gardenfieldId
   * @returns GardenField
   * @throws NotFound
   */
  public getGardenField(gardenfieldId: number): Promise<IGardenField> {
    return new Promise<any>((resolve, reject) => {
      gardenFieldClient
        .getGardenField(gardenfieldId)
        .then(result => {
          resolve(result.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public getGardenFieldImages(gardenfieldId: number): Promise<string[]> {
    return gardenFieldClient.getImageNames(gardenfieldId).then(result => {
      return result.data.map(fileName => this.parseImageNameToImageUrl(gardenfieldId, fileName));
    });
  }

  public getGardenFieldThumbnails(gardenfieldId: number): Promise<any> {
    return gardenFieldClient.getImageNames(gardenfieldId).then(result => {
      return result.data.map(fileName => this.parseImageNameToThumbnailUrl(gardenfieldId, fileName));
    });
  }

  public getGardenFieldCover(gardenfieldId: number): Promise<string> {
    return gardenFieldClient.getCoverImageName(gardenfieldId).then(result => {
      return this.parseImageNameToThumbnailUrl(gardenfieldId, result.data);
    });
  }

  public parseImageNameToImageUrl(gardenfieldId: number, imageName: string): string {
    if (process.env.NODE_ENV === 'production') {
      return IMAGE_BASE_URL + '/' + gardenfieldId + '/' + imageName;
    } else {
      return 'api/v1/gardenfields/' + gardenfieldId + '/downloadImage/' + imageName;
    }
  }

  public parseImageNameToThumbnailUrl(gardenfieldId: number, imageName: string): string {
    if (process.env.NODE_ENV === 'production') {
      return IMAGE_BASE_URL + '/' + gardenfieldId + '/thumbnails/' + imageName;
    } else {
      return 'api/v1/gardenfields/' + gardenfieldId + '/downloadThumbnail/' + imageName;
    }
  }
}
