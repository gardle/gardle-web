import { Component, Prop } from 'vue-property-decorator';
import Vue from 'vue';
import { Icon } from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { LMap, LMarker, LTileLayer } from 'vue2-leaflet';

delete Icon.Default.prototype._getIconUrl;

Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png')
});

@Component({
  components: {
    LMap,
    LTileLayer,
    LMarker
  }
})
export default class Map extends Vue {
  @Prop({ default: () => 16.363449 })
  longitude: number;
  @Prop({ default: () => 48.21 })
  latitude: number;

  get markerLocation(): number[] {
    return [this.latitude, this.longitude];
  }

  get locationIQTileProvider() {
    return {
      name: 'LocationIQ',
      visible: true,
      attribution: '&copy; <a target="_blank" href="https://locationiq.com/attribution">LocationIQ</a>',
      url: 'https://{s}-tiles.locationiq.com/v2/obk/r/{z}/{x}/{y}.png?key=' + process.env.LOCATION_IQ_KEY
    };
  }
}
