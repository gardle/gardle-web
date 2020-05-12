import axios from 'axios';
import { CreateLeasing } from '@/shared/model/leasing.model';
import { setHours, setMinutes } from 'date-fns';
import { ISimpleGardenField } from '@/shared/model/simpleGardenField.model';

const paymentClient = {
  getAccountSetupURL() {
    return axios.get('/api/v1/payments/accountLinkUrl');
  },
  createCheckoutSession(data) {
    return axios.post('/api/v1/payments/checkoutSession', data);
  }
};

export default class PaymentService {
  public getAccountSetupURL() {
    return new Promise<any>((resolve, reject) => {
      paymentClient
        .getAccountSetupURL()
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public createCheckoutSession(from: Date, to: Date, gardenField: ISimpleGardenField) {
    const leasing: CreateLeasing = new CreateLeasing(
      gardenField.id,
      gardenField.name,
      from.toISOString(),
      setMinutes(setHours(to, 23), 59).toISOString()
    );
    return new Promise<any>((resolve, reject) => {
      paymentClient
        .createCheckoutSession(leasing)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }
}
