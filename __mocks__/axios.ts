const mockAxios: any = jest.genMockFromModule('axios');

mockAxios.create = jest.fn(() => mockAxios);

export default mockAxios
