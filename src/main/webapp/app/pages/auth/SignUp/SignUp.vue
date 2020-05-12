<template>
    <v-row class="fill-height">
        <v-container class="fill-height">
            <v-row justify="center" align="center">
                <v-col cols="11" lg="7" class="text-center">
                    <h3 class="display-3" v-html="$t('register.title')">Join <span class="green--text font-weight-bold">gardle</span> today! </h3>
                    <p class="v-card__subtitle pa-0 mt-4" v-text="$t('register.subtitle')">Register an account and start renting and leasing gardenfields.</p>
                    <p class="v-card__subtitle pa-0 mt-4 grey--text" v-html="$t('register.disclaimer')">Please note, that we can currently offer our services only in
                        <span class="font-weight-medium">Austria</span> due to legal issues.</p>
                    <v-form>
                        <v-row justify="start">
                            <v-col cols="12">
                                <v-subheader v-text="$t('register.form.heading.baseInfo')">
                                    Base information
                                </v-subheader>
                            </v-col>
                            <v-col cols="12" md="6">
                                <v-text-field outlined
                                              :label="$t('register.form.label.firstname')"
                                              v-model="firstname"
                                              :error-messages="firstnameErrors"
                                              @input="$v.firstname.$touch()"
                                              @blur="$v.firstname.$touch()"></v-text-field>
                            </v-col>
                            <v-col cols="12" md="6">
                                <v-text-field outlined
                                              :label="$t('register.form.label.lastname')"
                                              v-model="lastname"
                                              :error-messages="lastnameErrors"
                                              @input="$v.lastname.$touch()"
                                              @blur="$v.lastname.$touch()"></v-text-field>
                            </v-col>
                            <v-col cols="12" lg="6">
                                <v-text-field outlined
                                              :label="$t('register.form.label.email')"
                                              type="email"
                                              v-model="email"
                                              :error-messages="emailErrors"
                                              @input="$v.email.$touch()"
                                              @blur="$v.email.$touch()"></v-text-field>
                            </v-col>
                            <v-col cols="12" lg="6">
                                <v-text-field outlined
                                              :label="$t('register.form.label.username')"
                                              type="text"
                                              v-model="username"
                                              counter="50"
                                              :error-messages="usernameErrors"
                                              @input="$v.username.$touch()"
                                              @blur="$v.username.$touch()"></v-text-field>
                            </v-col>
                            <v-col cols="12" lg="6">
                                <v-text-field outlined
                                              :label="$t('register.form.label.tel')"
                                              type="tel"
                                              v-model="tel"
                                              :error-messages="telErrors"
                                              @input="$v.tel.$touch()"
                                              @blur="$v.tel.$touch()"></v-text-field>
                            </v-col>
                            <v-col cols="12" lg="6">
                                <v-text-field outlined
                                              :label="$t('register.form.label.birthdate')"
                                              type="date"
                                              required
                                              :error-messages="birthdateErrors"
                                              v-model="birthdate"
                                              @input="$v.birthdate.$touch()"
                                              @blur="$v.birthdate.$touch()"></v-text-field>
                            </v-col>
                            <v-col cols="12">
                                <v-divider/>
                                <v-subheader class="mt-3" v-text="$t('register.form.heading.bankData')">
                                    Bank account information
                                </v-subheader>
                            </v-col>
                            <v-col cols="12" lg="6">
                                <v-text-field outlined
                                              :label="$t('register.form.label.iban')"
                                              type="text"
                                              required
                                              :error-messages="ibanErrors"
                                              v-model="iban"
                                              @input="$v.iban.$touch()"
                                              @blur="$v.iban.$touch()"
                                ></v-text-field>
                            </v-col>
                            <v-col cols="12">
                                <v-divider/>

                                <v-subheader class="mt-3" v-text="$t('register.form.heading.password')">
                                    Create password
                                </v-subheader>
                            </v-col>
                            <v-col cols="12" md="6">
                                <v-text-field outlined
                                              :label="$t('register.form.label.password')"
                                              v-model="password"
                                              type="password"
                                              :error-messages="passwordErrors"
                                              @input="$v.password.$touch()"
                                              @blur="$v.password.$touch()"></v-text-field>
                            </v-col>
                            <v-col cols="12" md="6">
                                <v-text-field outlined
                                              :label="$t('register.form.label.passwordConfirm')"
                                              v-model="passwordConfirm"
                                              type="password"
                                              :error-messages="passwordConfirmErrors"
                                              @input="$v.passwordConfirm.$touch()"
                                              @blur="$v.passwordConfirm.$touch()"></v-text-field>
                            </v-col>
                            <v-col cols="12">
                                <v-divider/>

                                <v-subheader class="mt-3" v-text="$t('register.form.heading.legal')">
                                    Legal
                                </v-subheader>
                            </v-col>
                            <v-col cols="12">
                                <v-checkbox
                                        :error-messages="gtcAcceptErrors"
                                        @change="$v.gtcAccept.$touch()"
                                        v-model="gtcAccept">
                                    <template v-slot:label>
                                        <span v-html="$t('register.form.acceptGTC')">
                                            I read and accept the terms and conditions.<br>
                                            I also acknowledge that this service is only available in Austria and that I only blabla.
                                        </span>
                                    </template>
                                </v-checkbox>
                            </v-col>
                            <v-col cols="12" class="text-right">
                                <v-btn text small class="grey--text mr-4 align-self-end"
                                       @click="handleHasAccount"
                                       v-text="$t('register.form.button.hasAccount')">I already have an account
                                </v-btn>
                                <v-menu v-model="eric" open-delay="3000" :open-on-hover="true" :open-on-click="false" :offset-y="true" top left>
                                    <template v-slot:activator="{on}">
                                        <v-btn class="primary" depressed x-large v-on="on" @click="submit" :disabled="$v.$invalid">
                                            {{(!eric) ? $t('register.form.button.register') : 'Let me IIIIN!'}}
                                            <v-icon right>
                                                mdi-chevron-right
                                            </v-icon>
                                        </v-btn>
                                    </template>
                                    <v-card class="pa-0">
                                        <video src="https://preview.redd.it/pdvuwinxq3a31.gif?format=mp4&s=b9d7f2631bca845943baa24f56020a9fcb86ba62"
                                               autoplay loop
                                               height="200"></video>
                                    </v-card>
                                </v-menu>
                            </v-col>
                        </v-row>
                    </v-form>
                </v-col>
            </v-row>
        </v-container>
    </v-row>
</template>

<script lang="ts" src="./SignUp.component.ts">
</script>

<style scoped>

</style>
