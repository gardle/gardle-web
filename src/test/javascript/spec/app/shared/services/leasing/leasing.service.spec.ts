import LeasingService from '@/shared/services/leasing/leasing.service';
import axios from 'axios';
import { addDays } from 'date-fns';
import { PageModel } from '@/shared/model/page.model';

const mockedAxios = axios as jest.Mocked<typeof axios>;

const mockedGardenFieldLeasings = [
  {
    id: 1,
    from: '2020-01-25T23:00:00Z',
    to: '2020-02-16T22:59:00Z',
    status: 'OPEN',
    user: {
      id: 55,
      login: 'testuser',
      firstName: 'Test',
      lastName: 'User',
      email: 'testmail@mail.com'
    },
    messages: [],
    gardenField: {
      id: 1,
      name: 'Eins Dankfield'
    }
  },
  {
    id: 2,
    from: '2020-01-25T23:00:00Z',
    to: '2020-02-16T22:59:00Z',
    status: 'RESERVED',
    user: {
      id: 55,
      login: 'testuser',
      firstName: 'Test',
      lastName: 'User',
      email: 'testmail@mail.com'
    },
    messages: [],
    gardenField: {
      id: 5,
      name: 'Testfield'
    }
  }
];

const generateDateRanges = count => {
  const res = [];
  for (let i = 0; i < count; ++i) {
    res.push({
      from: new Date().toISOString(),
      to: addDays(new Date(), Math.round(Math.random() * 10)).toISOString()
    });
  }
};

const leasingService = new LeasingService();

describe('leasing service', () => {
  it('should get leasing date ranges', async () => {
    const generatedDateRanges = generateDateRanges(4);
    mockedAxios.get.mockReturnValue(
      Promise.resolve({
        data: generatedDateRanges
      })
    );

    let func = async () => {
      return await leasingService.getLeasingDateRanges(1);
    };

    expect(await func()).toEqual(generatedDateRanges);
    expect(mockedAxios.get).toHaveBeenCalledWith('/api/v1/leasings/1/leasedDateRanges');

    const errResponse = {
      data: {},
      status: 500
    };

    mockedAxios.get.mockReturnValueOnce(
      Promise.reject({
        response: errResponse
      })
    );

    func = async () => {
      return await leasingService.getLeasingDateRanges(1);
    };

    await expect(func()).rejects.toEqual(errResponse);
    expect(mockedAxios.get).toHaveBeenCalledWith('/api/v1/leasings/1/leasedDateRanges');
  });

  it('should get leasings for gardenfield', async () => {
    mockedAxios.get.mockReturnValueOnce(
      Promise.resolve({
        data: [mockedGardenFieldLeasings[0]]
      })
    );

    const page = new PageModel(0, 5);

    const func = async () => {
      return await leasingService.getForGardenField(page, 1, null);
    };

    expect(await func()).toEqual([mockedGardenFieldLeasings[0]]);
    expect(mockedAxios.get).toHaveBeenCalledWith('/api/v1/gardenfields/1/leasings?page=0&size=5');
  });

  it('should get leasings for owner', async () => {
    mockedAxios.get.mockReturnValueOnce(
      Promise.resolve({
        data: mockedGardenFieldLeasings
      })
    );

    const page = new PageModel(0, 5);

    const func = async () => {
      return await leasingService.getForOwner(page, 1, null);
    };

    expect(await func()).toEqual(mockedGardenFieldLeasings);
    expect(mockedAxios.get).toHaveBeenCalledWith('/api/v1/users/1/leasings?page=0&size=5');
  });

  it('should get user listing leasings', async () => {
    mockedAxios.get.mockReturnValueOnce(
      Promise.resolve({
        data: mockedGardenFieldLeasings
      })
    );

    let func = async () => {
      return await leasingService.getUserListingLeasings(null);
    };

    expect(await func()).toEqual(mockedGardenFieldLeasings);
    expect(mockedAxios.get).toHaveBeenCalledWith('/api/v1/gardenfields/user/leasings');

    mockedAxios.get.mockReturnValueOnce(
      Promise.resolve({
        data: []
      })
    );

    func = async () => {
      return await leasingService.getUserListingLeasings({
        leasingStatus: 'OPEN',
        number: 1,
        size: 5
      });
    };

    expect(await func()).toEqual([]);
    expect(mockedAxios.get).toHaveBeenCalledWith('/api/v1/gardenfields/user/leasings?leasingStatus=OPEN&number=1&size=5');
  });

  it('should handle accept leasing request correctly', async () => {
    mockedAxios.put.mockReturnValueOnce(
      Promise.resolve({
        data: {}
      })
    );

    const leasingId = 1;
    const gardenFieldId = 3;

    let func = async () => {
      return await leasingService.acceptLeasingRequest(leasingId, gardenFieldId);
    };

    await expect(func()).resolves.toEqual({});
    expect(mockedAxios.put).toHaveBeenCalledWith('/api/v1/leasings/', { id: leasingId, gardenFieldId, status: 'RESERVED' });

    mockedAxios.put.mockReturnValueOnce(
      Promise.reject({
        response: {}
      })
    );

    func = async () => {
      return await leasingService.acceptLeasingRequest(leasingId, gardenFieldId);
    };

    await expect(func()).rejects.toEqual({});
    expect(mockedAxios.put).toHaveBeenCalledWith('/api/v1/leasings/', { id: leasingId, gardenFieldId, status: 'RESERVED' });
  });

  it('should handle reject leasing request correctly', async () => {
    mockedAxios.put.mockReturnValueOnce(
      Promise.resolve({
        data: {}
      })
    );

    const leasingId = 1;
    const gardenFieldId = 3;

    let func = async () => {
      return await leasingService.rejectLeasingRequest(leasingId, gardenFieldId);
    };

    await expect(func()).resolves.toEqual({});
    expect(mockedAxios.put).toHaveBeenCalledWith('/api/v1/leasings/', { id: leasingId, gardenFieldId, status: 'REJECTED' });

    mockedAxios.put.mockReturnValueOnce(
      Promise.reject({
        response: {}
      })
    );

    func = async () => {
      return await leasingService.rejectLeasingRequest(leasingId, gardenFieldId);
    };

    await expect(func()).rejects.toEqual({});
    expect(mockedAxios.put).toHaveBeenCalledWith('/api/v1/leasings/', { id: leasingId, gardenFieldId, status: 'REJECTED' });
  });

  it('should handle cancel leasing request correctly', async () => {
    mockedAxios.put.mockReturnValueOnce(
      Promise.resolve({
        data: {}
      })
    );

    const leasingId = 1;
    const gardenFieldId = 3;

    let func = async () => {
      return await leasingService.cancelLeasingRequest(leasingId, gardenFieldId);
    };

    await expect(func()).resolves.toEqual({});
    expect(mockedAxios.put).toHaveBeenCalledWith('/api/v1/leasings/', { id: leasingId, gardenFieldId, status: 'CANCELLED' });

    mockedAxios.put.mockReturnValueOnce(
      Promise.reject({
        response: {}
      })
    );

    func = async () => {
      return await leasingService.cancelLeasingRequest(leasingId, gardenFieldId);
    };

    await expect(func()).rejects.toEqual({});
    expect(mockedAxios.put).toHaveBeenCalledWith('/api/v1/leasings/', { id: leasingId, gardenFieldId, status: 'CANCELLED' });
  });

  /*func = async () => {
      return await leasingService.rejectLeasingRequest(leasingId, gardenFieldId);
    };

    expect(await func()).toEqual({});
    expect(mockedAxios.get).toHaveBeenCalledWith('/api/v1/leasings/',
      { id : leasingId, gardenFieldId, status: 'REJECTED'});

    func = async () => {
      return await leasingService.cancelLeasingRequest(leasingId, gardenFieldId);
    };

    expect(await func()).toEqual({});
    expect(mockedAxios.get).toHaveBeenCalledWith('/api/v1/leasings/',
      { id : leasingId, gardenFieldId, status: 'CANCELLED'});*/
});
