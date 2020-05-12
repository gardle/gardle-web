<template>
    <v-dialog v-model="DialogOpen"
              max-width="1000"
              :fullscreen="$vuetify.breakpoint.xsOnly"
              transition="dialog-bottom-transition">
        <template v-slot:activator="{ on }">
            <v-container fluid pa-0>
                <v-card class="pa-2">
                    <v-carousel
                        :show-arrows="true"
                        :hide-delimiters="false"
                        v-model="PageIndex"
                        height="100%"
                        class="fill-height fill-width"
                        delimiter-icon="mdi-minus"
                    >
                        <v-carousel-item
                            v-for="(p, i) in splicesImages"
                            :key="i"
                        >
                            <v-row class="flex-nowrap justify-center">
                                <v-col
                                    v-for="(n, j) in p"
                                    :key="j"
                                    class="d-flex child-flex px-1 py-0"
                                    :cols="12 / imageGroupCount"
                                >
                                    <v-card flat tile dark class="d-flex" @click="imageClick(i,j)">
                                        <v-img
                                            :src="n"
                                            aspect-ratio="1"
                                            class="grey lighten-2"
                                        >
                                            <template v-slot:placeholder>
                                                <v-row
                                                    class="fill-height ma-0"
                                                    align="center"
                                                    justify="center"
                                                >
                                                    <v-progress-circular indeterminate color="grey lighten-5"></v-progress-circular>
                                                </v-row>
                                            </template>
                                        </v-img>
                                    </v-card>
                                </v-col>
                            </v-row>
                        </v-carousel-item>
                    </v-carousel>
                </v-card>
            </v-container>
        </template>
        <v-card class="fill-height ma-0">
            <v-btn absolute small dark
                   fab top left
                   color="green" style="top: 1.5vh" @click="closeDialog()">
                <v-icon>mdi-close</v-icon>
            </v-btn>
            <v-row
                class="ma-0"
                align="center"
                justify="center"
                style="height:90vh"
            >
                <v-carousel
                    v-model="ImageIndex"
                    :show-arrows="true"
                    :hide-delimiters="true"
                    height="100%"
                    v-if="images !== null && thumbnails !== null"
                >
                    <v-carousel-item
                        v-for="(n, i) in images"
                        :key="i"
                    >
                        <v-row class="fill-height"
                               align="center"
                               justify="center"
                               style="max-height:90vh">
                            <v-img
                                :src="n"
                                :lazy-src="thumbnails[i]"
                                :contain="true"
                                max-height="100%"
                            >
                                <template v-slot:placeholder>
                                    <v-row
                                        class="fill-height ma-0"
                                        align="center"
                                        justify="center"
                                    >
                                        <v-progress-circular indeterminate color="grey lighten-5"></v-progress-circular>
                                    </v-row>
                                </template>
                            </v-img>
                        </v-row>
                    </v-carousel-item>
                </v-carousel>
            </v-row>
        </v-card>
    </v-dialog>
</template>
<style>
    .v-carousel__controls {
        height: 20px;
    }
</style>
<script lang="ts" src="./ImageGallery.component.ts">
</script>
