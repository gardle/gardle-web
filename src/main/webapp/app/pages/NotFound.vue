<template>
    <v-row class="fill-height" no-gutters>
        <v-col cols="12">
            <v-sheet class="fill-height grey lighten-4 text-center pa-5" tile>
                <v-icon size="128" class="grey--text">
                    mdi-magnify
                </v-icon>
                <h3 class="display-1 grey--text" v-text="$t('notFound.title')"></h3>
                <p class="body-1 grey--text text--lighten-1" v-text="$t('notFound.description', [$route.path])"></p>
            </v-sheet>
        </v-col>
    </v-row>
</template>

<script lang="ts">
    import {Component} from 'vue-property-decorator';
    import Vue from 'vue';

    @Component
    export default class PageNotFound extends Vue {
    }
</script>

<style scoped>

</style>
