<template>
    <div>
        <v-row>
            <template v-for="(image, index) in existingImages" v-if="existingImages && existingImages.length > 0">
                <v-sheet :height="150" :key="index"
                         :width="150"
                         class="d-flex relative mr-2">
                    <div class="absolute" style="top:5px; right:5px; z-index: 2" x-small>
                        <v-btn @click="deleteExistingImage(image)" class="white grey--text" icon>
                            <v-icon>
                                mdi-close
                            </v-icon>
                        </v-btn>
                    </div>
                    <v-img :max-height="150" :src="image.path"/>
                </v-sheet>
            </template>
            <template v-for="(image, index) in images" v-if="images.length > 0">
                <v-sheet :height="150" :key="index"
                         :width="150"
                         class="d-flex relative mr-2">
                    <div class="absolute" style="top:5px; right:5px; z-index: 2" x-small>
                        <v-btn @click="deleteImage(index)" class="white grey--text" icon>
                            <v-icon>
                                mdi-close
                            </v-icon>
                        </v-btn>
                    </div>
                    <v-img :max-height="150" :src="image.path"/>
                </v-sheet>
            </template>
            <label :for="uploaderId" class="d-flex">
                <v-sheet :height="150"
                         :width="150"
                         class="grey lighten-3 cursor-pointer d-inline-block">
                    <v-row align="center" class="fill-height" justify="center">
                        <v-icon class="grey--text text--darken-1" size="56">
                            mdi-plus
                        </v-icon>
                    </v-row>
                </v-sheet>
            </label>
        </v-row>
        <input :accept="acceptTypes" :id="uploaderId" :multiple="true" @change="addImage" class="d-none" name="images"
               type="file">
    </div>
</template>

<script lang="ts" src="./imageUpload.component.ts"></script>

<style scoped>

</style>
