<template>
    <v-row>
        <v-container>
            <v-row align="start" justify="center">
                <v-col cols="11" sm="9" lg="8" class="mb-10">
                    <h3 class="display-1" v-text="$t('activate.title')">Activate account</h3>
                </v-col>
                <v-col cols="11" sm="9" lg="6" class="text-center">
                    <v-card outlined class="info" v-if="!hasActivationKey">
                        <v-card-text v-html="$t('activate.messages.noActivationKey')">
                            In order to activate your account, please check your emails (and spam if needed and click the activation link.<br>
                            If you have not gotten any email, please contact our support.
                        </v-card-text>
                    </v-card>
                    <v-progress-circular color="grey" indeterminate v-else-if="loading === true"/>
                    <v-card outlined class="success" v-else-if="activated !== null && activated === true">
                        <v-card-text v-html="$t('activate.messages.success')">
                            Your account has been successfully activated, you can now use Gardle :)<br>
                            To start you can login and explore the whole app.
                        </v-card-text>
                        <v-card-actions>
                            <v-spacer/>
                            <v-btn text color="white" @click="handleLoginClick" v-text="$t('activate.loginButton')">
                                Login
                            </v-btn>
                        </v-card-actions>
                    </v-card>
                    <v-card outlined class="orange lighten-2" v-else-if="activated !== null && activated === false">
                        <v-card-text v-html="$t('activate.messages.error')">
                            We're very sorry, but it seems we couldnt activate your account :(
                            Please contact our support for further help.
                        </v-card-text>
                    </v-card>
                </v-col>
            </v-row>
        </v-container>
    </v-row>
</template>

<script lang="ts" src="./Activate.component.ts"></script>

<style scoped>

</style>
