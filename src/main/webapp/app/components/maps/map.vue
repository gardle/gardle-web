<template>
    <LMap :center="markerLocation"
          :zoom="10"
          :min-zoom="5"
          :max-zoom="18"
          style="z-index: 0"
    >
        <l-tile-layer
            :key="locationIQTileProvider.name"
            :name="locationIQTileProvider.name"
            :visible="locationIQTileProvider.visible"
            :url="locationIQTileProvider.url"
            :attribution="locationIQTileProvider.attribution"
            layer-type="base"
        />
        <l-marker :lat-lng.sync="markerLocation"/>
    </LMap>
</template>
<script lang="ts" src="./map.component.ts"></script>
