export interface ICreateListing {
  name: string;
  description?: string;
  sizeInM2: number;
  pricePerM2: number;
  roofed: boolean;
  glassHouse: boolean;
  high: boolean;
  water: boolean;
  electricity: boolean;
  phValue?: number;
  latitude: number;
  longitude: number;
  city: string;
}

export class CreateListing implements ICreateListing {
  public name: string;
  public sizeInM2: number;
  public pricePerM2: number;
  public roofed = false;
  public glassHouse = false;
  public high = false;
  public water = false;
  public electricity = false;
  public phValue?: number = null;
  public description?: string;
  public city: string;
  public latitude: number;
  public longitude: number;
}
