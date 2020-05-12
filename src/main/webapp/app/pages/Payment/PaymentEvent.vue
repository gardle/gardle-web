<template>
    <v-row justify="center" align="center" class="fill-height">
        <v-col cols="12" sm="8" lg="6">
            <v-sheet v-if="isSuccess" class="green lighten-2 green--text text--darken-4 pa-5">
                <div class="py-4 d-flex align-center justify-start">
                    <v-icon size="24" class="pr-3 green--text text--darken-4">
                        mdi-check
                    </v-icon>
                    <h3 class="headline" v-html="$t('payment.events.success.title')"></h3>
                </div>
                <p class="caption" v-html="$t('payment.events.success.description')"></p>
            </v-sheet>
            <v-sheet v-else class="deep-orange darken-2 darken-1 white--text pa-5">
                <div class="py-4 d-flex align-center justify-start">
                    <v-icon size="24" class="pr-3 white--text">
                        mdi-close
                    </v-icon>
                    <h3 class="headline" v-html="$t('payment.events.error.title')"></h3>
                </div>
                <p class="caption" v-html="$t('payment.events.error.description')"></p>
            </v-sheet>
        </v-col>
    </v-row>
</template>

<script lang="ts">
    import {Component} from 'vue-property-decorator';
    import Vue from 'vue';

    @Component
    export default class PagePaymentEvent extends Vue {

        get isSuccess() {
            return this.urlAction === 'success';
        }

        get urlAction() {
            return this.$route.path.split('/').reverse()[0]
        }

        beforeRouteEnter() {
            console.log('whaat')
        }

    }
</script>

<style scoped>

</style>
