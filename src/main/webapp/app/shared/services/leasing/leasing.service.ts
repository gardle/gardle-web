import axios from 'axios';
import { LeasingDateRange } from '@/shared/model/leasingDateRange.model';
import { PageModel } from '@/shared/model/page.model';
import { serializeUriComponents } from '@/shared/services/util/uriComponents.service';

const leasingClient = {
  getLeasingDateRanges(gardenFieldId) {
    return axios.get(`/api/v1/leasings/${gardenFieldId}/leasedDateRanges`);
  },
  update(leasingId: number, gardenFieldId: number, status: string) {
    return axios.put(`/api/v1/leasings/`, {
      id: leasingId,
      gardenFieldId,
      status
    });
  },
  getForGardenField(page: PageModel, gardenFieldId: number, filters?: object) {
    return axios.get(
      `/api/v1/gardenfields/${gardenFieldId}/leasings?page=${page.pageNumber}&size=${page.pageSize}${
        filters ? '&' + serializeUriComponents(filters) : ''
      }`
    );
  },
  getForUser(page: PageModel, userId: number, filters?: object) {
    return axios.get(
      `/api/v1/users/${userId}/leasings?page=${page.pageNumber}&size=${page.pageSize}${
        filters ? '&' + serializeUriComponents(filters) : ''
      }`
    );
  },
  getUserListingLeasings(filters?: object) {
    return axios.get(`/api/v1/gardenfields/user/leasings${filters ? '?' + serializeUriComponents(filters) : ''}`);
  }
};

export default class LeasingService {
  public getLeasingDateRanges(gardenFieldId: number): Promise<LeasingDateRange[]> {
    return new Promise<any>((resolve, reject) => {
      leasingClient
        .getLeasingDateRanges(gardenFieldId)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public getForGardenField(page: PageModel, gardenFieldId: number, filters: object) {
    return new Promise<any>((resolve, reject) => {
      leasingClient
        .getForGardenField(page, gardenFieldId, filters)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public getForOwner(page: PageModel, userId: number, filters?: Object) {
    return new Promise<any>((resolve, reject) => {
      leasingClient
        .getForUser(page, userId, filters)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public getUserListingLeasings(filters?: object) {
    return new Promise<any>((resolve, reject) => {
      leasingClient
        .getUserListingLeasings(filters)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public acceptLeasingRequest(leasingId: number, gardenFieldId: number) {
    return new Promise<any>((resolve, reject) => {
      leasingClient
        .update(leasingId, gardenFieldId, 'RESERVED')
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public rejectLeasingRequest(leasingId: number, gardenFieldId: number) {
    return new Promise<any>((resolve, reject) => {
      leasingClient
        .update(leasingId, gardenFieldId, 'REJECTED')
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  public cancelLeasingRequest(leasingId: number, gardenFieldId: number) {
    return new Promise<any>((resolve, reject) => {
      leasingClient
        .update(leasingId, gardenFieldId, 'CANCELLED')
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }
}
