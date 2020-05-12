<template>
    <v-row justify="center" v-if="account !== null">
        <v-col cols="12" class="py-0 grey lighten-3">
            <v-row justify="center" no-gutters>
                <v-col cols="11" md="9">
                    <v-toolbar elevation="0" class="transparent">
                        <v-toolbar-title>{{$t('settings.title')}}</v-toolbar-title>
                        <v-spacer/>
                    </v-toolbar>
                </v-col>
            </v-row>
        </v-col>
        <v-col cols="11" sm="10" lg="8" xl="7">
            <v-subheader>{{$t('settings.form.personalData')}}</v-subheader>
            <v-row dense>

                <v-col cols="12" md="6">
                    <v-text-field outlined
                                  :label="$t('settings.form.firstname')"
                                  v-model="account.firstName"
                                  :error-messages="firstnameErrors"
                                  @input="$v.account.firstName.$touch()"
                                  @blur="$v.account.firstName.$touch()">
                    </v-text-field>
                </v-col>
                <v-col cols="12" md="6">
                    <v-text-field outlined
                                  :label="$t('settings.form.lastname')"
                                  v-model="account.lastName"
                                  :error-messages="lastnameErrors"
                                  @input="$v.account.lastName.$touch()"
                                  @blur="$v.account.lastName.$touch()"></v-text-field>
                </v-col>
                <v-col cols="12" md="6">
                    <v-text-field outlined
                                  :label="$t('settings.form.birthDate')"
                                  type="date"
                                  v-model="account.birthDate"
                                  :error-messages="birthDateErrors"
                                  @input="$v.account.birthDate.$touch()"
                                  @blur="$v.account.birthDate.$touch()"></v-text-field>
                </v-col>
                <v-col cols="12" md="6">
                    <v-text-field outlined
                                  :label="$t('settings.form.tel')"
                                  type="tel"
                                  v-model="account.tel"
                                  :error-messages="telErrors"
                                  @input="$v.account.tel.$touch()"
                                  @blur="$v.account.tel.$touch()"></v-text-field>
                </v-col>
            </v-row>

            <v-subheader>{{$t('settings.form.email')}}</v-subheader>

            <v-text-field outlined
                          :label="$t('settings.form.email')"
                          v-model="account.email"
                          :error-messages="emailErrors"
                          @input="$v.account.email.$touch()"
                          @blur="$v.account.email.$touch()"></v-text-field>

            <v-subheader>{{$t('settings.form.language')}}</v-subheader>
            <v-select :label="$t('settings.form.selectedLanguage')" outlined v-model="account.langKey" :items="languageKeys"></v-select>

            <v-btn color="primary" depressed :disabled="$v.account.$invalid" @click="saveAccountData">{{$t('settings.form.saveButton')}}</v-btn>
        </v-col>

        <v-col cols="11" sm="10" lg="8" xl="7">
            <v-divider class="my-4"/>
            <v-subheader>{{$t('settings.form.changePassword')}}</v-subheader>

            <v-row no-gutters>
                <v-col cols="12">
                    <v-text-field outlined
                                  :label="$t('settings.form.currentPassword')"
                                  type="password"
                                  v-model="passwordChange.currentPassword"
                                  :error-messages="currentPasswordErrors"
                                  @input="$v.passwordChange.currentPassword.$touch()"
                                  @blur="$v.passwordChange.currentPassword.$touch()"></v-text-field>
                </v-col>

            </v-row>
            <v-row dense>
                <v-col cols="12" md="6">
                    <v-text-field outlined
                                  :label="$t('settings.form.newPassword')"
                                  type="password"
                                  v-model="passwordChange.newPassword"
                                  :error-messages="newPasswordErrors"
                                  @input="$v.passwordChange.newPassword.$touch()"
                                  @blur="$v.passwordChange.newPassword.$touch()"></v-text-field>
                </v-col>
                <v-col cols="12" md="6">
                    <v-text-field outlined
                                  :label="$t('settings.form.repeatPassword')"
                                  type="password"
                                  v-model="passwordChange.repeatPassword"
                                  :error-messages="repeatPasswordErrors"
                                  @input="$v.passwordChange.repeatPassword.$touch()"
                                  @blur="$v.passwordChange.repeatPassword.$touch()"></v-text-field>
                </v-col>
            </v-row>

            <v-btn color="error" @click="changePassword" :disabled="$v.passwordChange.$invalid" depressed>
                {{$t('settings.form.changePasswordButton')}}
            </v-btn>
        </v-col>

    </v-row>
</template>

<script lang="ts" src="./Settings.component.ts"></script>

<style scoped>

</style>
