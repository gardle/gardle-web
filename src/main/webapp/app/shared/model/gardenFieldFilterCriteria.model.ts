import { ILocationFilterCriteria } from '@/shared/model/locationFilterCritera.model';

export interface IGardenFieldFilterCriteria {
  locationFilterCriteria?: ILocationFilterCriteria;
  minPricePerM2?: number;
  maxPricePerM2?: number;
  sizeInM2LowerBound?: number;
  sizeInM2UpperBound?: number;
  roofed?: boolean;
  electricity?: boolean;
  high?: boolean;
  glassHouse?: boolean;
  water?: boolean;
  leasingTimeFrom?: Date;
  leasingTimeTo?: Date;
  keywords?: string;

  getAsFilterString(): string;
}

export class GardenFieldFilterCriteria implements IGardenFieldFilterCriteria {
  constructor(
    public locationFilterCriteria?: ILocationFilterCriteria,
    public minPricePerM2?: number,
    public maxPricePerM2?: number,
    public sizeInM2LowerBound?: number,
    public sizeInM2UpperBound?: number,
    public roofed?: boolean,
    public electricity?: boolean,
    public high?: boolean,
    public glassHouse?: boolean,
    public water?: boolean,
    public leasingTimeFrom?: Date,
    public leasingTimeTo?: Date,
    public keywords?: string
  ) {}

  public getAsFilterString(): string {
    let locationCriteriaString = '';
    let filterCriteriaString = '';
    if (this.locationFilterCriteria) {
      locationCriteriaString = ''.concat(
        this.locationFilterCriteria.latitude ? 'latitude=' + this.locationFilterCriteria.latitude + '&' : '',
        this.locationFilterCriteria.longitude ? 'longitude=' + this.locationFilterCriteria.longitude + '&' : '',
        this.locationFilterCriteria.radius ? 'radiusInKM=' + this.locationFilterCriteria.radius + '&' : ''
      );
    }
    filterCriteriaString = ''.concat(
      this.minPricePerM2 ? 'minPrice=' + this.minPricePerM2.toString() + '&' : '',
      this.maxPricePerM2 ? 'maxPrice=' + this.maxPricePerM2.toString() + '&' : '',
      this.sizeInM2LowerBound ? 'sizeInM2LowerBound=' + this.sizeInM2LowerBound.toString() + '&' : '',
      this.sizeInM2UpperBound ? 'sizeInM2UpperBound=' + this.sizeInM2UpperBound.toString() + '&' : '',
      this.roofed ? 'roofed=' + this.roofed.toString() + '&' : '',
      this.electricity ? 'electricity=' + this.electricity.toString() + '&' : '',
      this.high ? 'high=' + this.high.toString() + '&' : '',
      this.glassHouse ? 'glassHouse=' + this.glassHouse.toString() + '&' : '',
      this.water ? 'water=' + this.water.toString() + '&' : '',
      this.leasingTimeFrom ? 'leasingTimeFrom=' + this.leasingTimeFrom.toISOString() + '&' : '', // toISOString for Instant compatibility
      this.leasingTimeTo ? 'leasingTimeTo=' + this.leasingTimeTo.toISOString() + '&' : '',
      this.keywords ? 'keywords=' + this.keywords.toString() : ''
    );
    let result = locationCriteriaString.concat(filterCriteriaString);
    if (result && result.endsWith('&')) {
      result = result.substr(0, result.length - 1);
    }
    return result;
  }
}
