import axios from 'axios';

const activateApiClient = {
  activate(key) {
    return axios.get(`api/v1/activate?key=${key}`);
  }
};

export default class ActivationService {
  public activate(key: string) {
    return new Promise((resolve, reject) => {
      activateApiClient
        .activate(key)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }
}
